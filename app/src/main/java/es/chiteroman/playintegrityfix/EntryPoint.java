package es.chiteroman.playintegrityfix;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.Provider;
import java.security.Security;

public class EntryPoint {
    public static void init() {
        spoofDevice();
        spoofProvider();
    }

    private static void spoofProvider() {
        final String KEYSTORE = "AndroidKeyStore";

        try {
            Provider provider = Security.getProvider(KEYSTORE);
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE);

            Field f = keyStore.getClass().getDeclaredField("keyStoreSpi");
            f.setAccessible(true);
            CustomKeyStoreSpi.keyStoreSpi = (KeyStoreSpi) f.get(keyStore);
            f.setAccessible(false);

            CustomProvider customProvider = new CustomProvider(provider);
            Security.removeProvider(KEYSTORE);
            Security.insertProviderAt(customProvider, 1);

            LOG("Spoof KeyStoreSpi and Provider done!");

        } catch (KeyStoreException e) {
            LOG("Couldn't find KeyStore: " + e);
        } catch (NoSuchFieldException e) {
            LOG("Couldn't find field: " + e);
        } catch (IllegalAccessException e) {
            LOG("Couldn't change access of field: " + e);
        }
    }

    public static void spoofDevice() {
        final String PRODUCT = "WW_Phone";
        final String DEVICE = "ASUS_X00HD_4";
        final String MANUFACTURER = "Asus";
        final String BRAND = "Asus";
        final String MODEL = "ASUS_X00HD";
        final String FINGERPRINT = "asus/WW_Phone/ASUS_X00HD_4:7.1.1/NMF26F/14.2016.1801.372-20180119:user/release-keys";

        setProp("PRODUCT", PRODUCT);
        setProp("DEVICE", DEVICE);
        setProp("MANUFACTURER", MANUFACTURER);
        setProp("BRAND", BRAND);
        setProp("MODEL", MODEL);

        setProp("FINGERPRINT", FINGERPRINT);
    }

    private static void setProp(String name, String value) {
        try {
            String oldValue;
            Field field = Build.class.getDeclaredField(name);
            field.setAccessible(true);
            oldValue = (String) field.get(null);
            field.set(null, value);
            field.setAccessible(false);
            LOG(String.format("[%s] -> [%s] is now: [%s]", name, oldValue, value));
        } catch (NoSuchFieldException e) {
            LOG(String.format("Couldn't find '%s' field name.", name));
        } catch (IllegalAccessException e) {
            LOG(String.format("Couldn't modify '%s' field value.", name));
        }
    }

    public static void LOG(String msg) {
        Log.d("PIF/Java", msg);
    }
}