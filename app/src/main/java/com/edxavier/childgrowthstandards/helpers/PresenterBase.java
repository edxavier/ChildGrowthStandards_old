package com.edxavier.childgrowthstandards.helpers;

/**
 * @author  : Eder Xavier Rojas
 * @date    : 26/08/2016 - 00:55
 * @package : com.vynil.domain.baseCotracts
 * @project : Vynil
 */
public interface PresenterBase {
    void onCreate();
    void onResume();
    void onPause();
    void onDestroy();
}