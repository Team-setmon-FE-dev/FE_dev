package com.setmon.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt가 앱 전체의 의존성 주입을 시작하는 지점. AndroidManifest의 android:name에 등록해야 한다. */
@HiltAndroidApp
class SetmonApplication : Application()
