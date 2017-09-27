def can_build(plat):
	return plat=="android"

def configure(env):
	if (env['platform'] == 'android'):
		env.android_add_java_dir("android/src")
		env.android_add_maven_repository("url 'https://raw.githubusercontent.com/onepf/OPF-mvn-repo/master/'")
		env.android_add_to_manifest("android/AndroidManifestChunk.xml")
		env.android_add_to_permissions("android/AndroidManifestPermissionsChunk.xml")
		env.android_add_dependency("compile 'org.onepf:openiab:0.9.8.4'")
		env.android_add_dependency("compile 'com.amazon:in-app-purchasing:2.0.1'")
		env.android_add_dependency("compile 'com.braintree:fortumo-in-app:9.1.2'")
