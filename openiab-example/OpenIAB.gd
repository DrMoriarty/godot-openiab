extends Node

# set appstore keys there
const allKeys = {
#    'com.google.play':  '',
#    'Appland':          '',
#    'cm.aptoide.pt':    '',
#    'SlideME':          '',
#    'com.yandex.store': ''
}

# set your skus there
const more_skus = ['sku1', 'sku2']

signal iab_owned(sku)
signal iab_consumed(sku)
signal iab_purchased(sku)

var iab = null

func _ready():
    if Globals.has_singleton("OpenIAB"):
        iab = Globals.get_singleton("OpenIAB")
        iab.registerCallback('inited', 'iab_inited')
        iab.registerCallback('owned', 'iab_owned')
        iab.registerCallback('consumed', 'iab_consumed')
        iab.registerCallback('purchased', 'iab_purchased')
        iab.init(get_instance_ID(), allKeys)
        Log.info('OpenIAB plugin inited')
    else:
        Log.warning('OpenIAB plugin not found!')

func iab_inited(result):
    Log.info('iab inited: %s'%result)
    if result == '0':
        Log.info('load skus: %s'%var2str(more_skus))
        iab.queryInventory(more_skus)

func iab_owned(sku):
    Log.info('owned %s'%sku)
    emit_signal('iab_owned', sku)

func iab_consumed(sku):
    Log.info('consumed %s'%sku)
    emit_signal('iab_consumed', sku)

func iab_purchased(sku):
    Log.info('purchased %s'%sku)
    emit_signal('iab_purchased', sku)

func map_yandex_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapYandexSku(sku, new_sku)
        
func map_nokia_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapNokiaSku(sku, new_sku)

func map_amazon_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapAmazonSku(sku, new_sku)

func map_appland_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapApplandSku(sku, new_sku)

func map_slideme_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapSlidemeSku(sku, new_sku)

func map_samsung_skus(skus):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    for sku in skus:
        var new_sku = skus[sku]
        iab.mapSamsungSku(sku, new_sku)

func sku_info(sku):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    var info = iab.skuInfo(sku)
    return info

func purchase(sku):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    iab.purchase(sku)

func consume(sku):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    iab.consume(sku)

func debug(showlog):
    if iab == null:
        Log.warning('OpenIAB plugin not found!')
        return
    if showlog:
        iab.enableLogging()

