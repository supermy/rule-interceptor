package com.supermy.flume.interceptor;

/**
 * Created by moyong on 17/1/4.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *Java 內建 AES 演算法的 API，所以在 Groovy 程式中，實際不到十行程式碼，就能完成加密或解密字串的處理。

 為了把加密結果轉成十六進位，我們使用 commons-codec 提供的 HEX 物件。當然在 Groovy 只要使用 Grab / Grapes 就可以輕易自動載入需要的函式庫檔案。
 *
 */
public class AESJdk {

    public static void main(String[] args) throws Exception {
        String text = "I Love China";

        //設定金鑰與演算法
        SecretKeySpec key = new SecretKeySpec("0123456789012345".getBytes(), "AES");
        Cipher aes1 = Cipher.getInstance("AES");

        //加密
        aes1.init(Cipher.ENCRYPT_MODE, key);
        String e_text = new String(Hex.encodeHex(aes1.doFinal(text.getBytes("UTF-8"))));

        //解密
        aes1.init(Cipher.DECRYPT_MODE, key);
        String text1 = new String(aes1.doFinal(Hex.decodeHex(e_text.toCharArray())));


        System.out.println("before......text:"+text);
        System.out.println("before......key:"+key);

        System.out.println("done......text:"+e_text);
        System.out.println("after......text:"+text1);

    }
}
