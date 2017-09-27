package org.godotengine.godot;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.SkuManager;
import org.onepf.oms.util.Logger;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;
import org.onepf.oms.appstore.googleUtils.SkuDetails;

public class GodotOpenIAB extends Godot.SingletonBase {
    //variable
    private Activity activity = null;
    private int instanceId = 0;
    private Toast toast;
    private final String TAG = "OpenIAB";
    OpenIabHelper mHelper;
    Inventory mInventory;

    private HashMap<String, String> callbackFunctions;

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    static public Godot.SingletonBase initialize(Activity p_activity) {

        return new GodotOpenIAB(p_activity);
    }

    //constructor
    public GodotOpenIAB(Activity p_activity) {
        //The registration of this and its functions
        registerClass("OpenIAB", new String[]{
                "init", "registerCallback", "unregisterCallback",
                "mapYandexSku", "mapNokiaSku", "mapAmazonSku", "mapApplandSku", "mapSlidemeSku", "mapSamsungSku", "mapGoogleSku",
                "queryInventory", "skuInfo", "purchase", "consume", "enableLogging"
        });

        callbackFunctions = new HashMap<String, String>();

        activity = p_activity;
    }


    // Register callbacks to GDscript
    public void registerCallback(final String callback_type, final String callback_function) {
        callbackFunctions.put(callback_type, callback_function);
    }

    // Deregister callbacks to GDscript
    public void unregisterCallback(final String callback_type) {
        callbackFunctions.remove(callback_type);
    }

    // Run a callback to GDscript
    private void runCallback(final String callback_type, final Object argument) {
        if (callbackFunctions.containsKey(callback_type)) {
            GodotLib.calldeferred(instanceId, callbackFunctions.get(callback_type), new Object[]{ argument });
        }
    }

    //initialization of OpenIAB
    public void init(final int new_instanceId, final Dictionary appStoreKeys) {
        instanceId = new_instanceId;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> STORE_KEYS_MAP = new HashMap<>();
                for(String key: appStoreKeys.get_keys()) {
                    STORE_KEYS_MAP.put(key, appStoreKeys.get(key).toString());
                }
                OpenIabHelper.Options.Builder builder = new OpenIabHelper.Options.Builder()
                    .setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_INSTALLER_THEN_BEST_FIT)
                    .setVerifyMode(OpenIabHelper.Options.VERIFY_EVERYTHING)
                    .addStoreKeys(STORE_KEYS_MAP);
                mHelper = new OpenIabHelper(activity, builder.build());
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                        public void onIabSetupFinished(IabResult result) {
                            runCallback("inited", Integer.toString(result.getResponse()));
                            if (!result.isSuccess()) {
                                showToast("Problem setting up in-app billing: " + result);
                                return;
                            }
                            Log.d(TAG, "Setup successful.");
                        }
                    });
            }
        });
    }
    
    public void queryInventory(String[] skus) {
        Log.d(TAG, "Querying inventory.");
        List<String> moreSkus = Arrays.asList(skus);
        mHelper.queryInventoryAsync(true, moreSkus, mGotInventoryListener);
    }

    public void mapYandexSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_YANDEX, newSku);
    }

    public void mapNokiaSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_NOKIA, newSku);
    }

    public void mapAmazonSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_AMAZON, newSku);
    }

    public void mapApplandSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_APPLAND, newSku);
    }

    public void mapSlidemeSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_SLIDEME, newSku);
    }

    public void mapSamsungSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_SAMSUNG, newSku);
    }

    public void mapGoogleSku(final String sku, final String newSku) {
        SkuManager.getInstance().mapSku(sku, OpenIabHelper.NAME_GOOGLE, newSku);
    }

    public Dictionary skuInfo(final String sku) {
        Dictionary item = new Dictionary();
        if(mInventory != null) {
            SkuDetails details = mInventory.getSkuDetails(sku);
            if(details != null) {
                item.put("sku", details.getSku());
                item.put("type", details.getType());
                item.put("price", details.getPrice());
                item.put("title", details.getTitle());
                item.put("description", details.getDescription());
                item.put("itemType", details.getItemType());
            }
        }
        return item;
    }

    public void purchase(final String sku) {
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";
        mHelper.launchPurchaseFlow(activity, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
    }

    public void consume(final String sku) {
        if(mInventory != null) {
            Purchase purchase = mInventory.getPurchase(sku);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } else {
                Log.w(TAG, "Don't have purchase "+sku+" for consuming");
            }
        }
    }

    public void enableLogging() {
        Logger.setLoggable(true);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    Log.d(TAG, "Query inventory finished.");
                    if (result.isFailure()) {
                        showToast("Failed to query inventory: " + result);
                        return;
                    }

                    Log.d(TAG, "Query inventory was successful.");
                    mInventory = inventory;
                    List<String> skus = inventory.getAllOwnedSkus();
                    for(String sku: skus) {
                        Log.d(TAG, "Owned "+sku);
                        runCallback("owned", sku);
                    }
                }
            };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

                if (result.isSuccess()) {
                    runCallback("consumed", purchase.getSku() );
                    Log.d(TAG, "Consumption successful. Provisioning.");
                } else {
                    showToast("Error while consuming: " + result);
                }
                Log.d(TAG, "End consumption flow.");
            }
        };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                showToast("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                showToast("Error purchasing. Authenticity verification failed.");
                return;
            }
            Log.d(TAG, "Purchase successful.");
            runCallback("purchased", purchase.getSku());
        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    private void showToast(final String text) {
        if (toast == null) {
            toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override protected void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
        mHelper.handleActivityResult(requestCode, resultCode, data);
    }

}
