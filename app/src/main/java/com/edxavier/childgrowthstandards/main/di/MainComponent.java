package com.edxavier.childgrowthstandards.main.di;

import com.edxavier.childgrowthstandards.SplashScreen;
import com.edxavier.childgrowthstandards.libs.AppModule;
import com.edxavier.childgrowthstandards.main.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eder Xavier Rojas on 15/08/2016.
 */
@Singleton
@Component(modules = {AppModule.class})
public interface MainComponent {
    void inject(MainActivity activity);
    void inject(SplashScreen activity);

}
