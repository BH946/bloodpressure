package mica.part1.checkMate.SharedPreference

import android.app.Application

// https://riapapa-collection.tistory.com/41
// https://leveloper.tistory.com/133
// 핵심은 이부분이 onCreate되기전 먼저 실행되어야 하기때문에 ManiFest에 name을 추가해준다.

class MyApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}

