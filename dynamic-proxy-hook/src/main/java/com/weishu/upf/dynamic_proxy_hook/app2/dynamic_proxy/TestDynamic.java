package com.weishu.upf.dynamic_proxy_hook.app2.dynamic_proxy;

import android.util.Log;

import com.weishu.upf.dynamic_proxy_hook.app2.Shopping;
import com.weishu.upf.dynamic_proxy_hook.app2.ShoppingImpl;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author weishu
 * @date 16/1/28
 */
public class TestDynamic {
    public static void main(String[] args) {
        Shopping women = new ShoppingImpl();

        // 正常购物
        Log.v("sanbo.TestDynamic", "正常购物 : " + Arrays.toString(women.doShopping(100)));

        // 招代理
        women = (Shopping) Proxy.newProxyInstance(Shopping.class.getClassLoader(),
                women.getClass().getInterfaces(), new ShoppingHandler(women));

        System.out.println(Arrays.toString(women.doShopping(100)));
    }
}
