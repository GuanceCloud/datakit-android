package com.ft.sdk;

/**
 * In the automatic buried point function, filter the addresses that do not need to be collected, generally used to exclude some requests that are not related to business
 *
 * @author Brandon
 */
public interface FTInTakeUrlHandler {
    /**
     * Whether to collect this address data
     *
     * @param url url address, example url="https://www.guance.com/"
     * @return true not collect, false collect
     */
    boolean isInTakeUrl(String url);
}
