### 告诉你我知道的多渠道打包
多渠道打包有什么用我就不说了，其实还不是上面提出的需求，为了这些需求，那我们也只能依法实现了。下面给大家介绍一下我平时写的多渠道打包，
不过于深入研究其原理，因为在具体的我也说不出来，只是介绍实现的需求。
#### 需求
产品：给我打四个包，我要这四个包应用同时装到一个手机上，每个应用名不一样，应用图标不一样，打开软件后显示的也不一样。
码农：。。。呵呵
#### 新建项目
新建项目之后，修改工程最外层build.gradle文件，目的在于统一管理。

        //所有的版本编译工具在此定义
        ext {
            //SDK和编译工具的版本
            compileSdkVersion = 25
            buildToolsVersion = '25.0.0'
            minSdkVersion = 15
            targetSdkVersion = 25
            //项目依赖库的版本
            supportLibraryVersion = "25.0.0"
        }
如图：
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/step1.png?raw=true)
添加之后，自然要用它了，不然改了干啥，在module层级的build.gradle文件里android大括号里面修改一下原有配置。

        android{
            compileSdkVersion rootProject.ext.compileSdkVersion
            buildToolsVersion rootProject.ext.buildToolsVersion
            
            
            
                defaultConfig {
                    applicationId "com.example.huangcl.manychannel"
                    minSdkVersion rootProject.ext.minSdkVersion
                    targetSdkVersion rootProject.ext.targetSdkVersion
                    versionCode Integer.valueOf(VERSIONCODE)//VERSIONCODE，你会好奇这是哪里来的
                    versionName VERSIONNAME//VERSIONNAME这个也是
                    testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
                }
                ...
            }
            
        dependencies {
            compile fileTree(dir: 'libs', include: ['*.jar'])
            androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
                exclude group: 'com.android.support', module: 'support-annotations'
            })
            compile "com.android.support:appcompat-v7:$rootProject.ext.supportLibraryVersion"//在这里用supportLibraryVersion
            testCompile 'junit:junit:4.12'
        }

代码里面的两个值，是通过在app层级下新建的一个文件名字叫gradle.properties，这里你也可以叫其他，但一定要.properties后缀，因为Java本身对.properties文件支持。
然后在文件里面写：

        //版本信息
        VERSIONCODE=1
        VERSIONNAME=1.0.0
得是.properties结尾。
#### 多渠道步骤一：不同的应用名和图标
在清单配置文件里application层级中meta-data接入
    
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="com.example.huangcl.manychannel">
        
            <application
                android:allowBackup="true"
                android:icon="${app_icon}"//图标不同不能写死了
                android:label="${app_name}"//同
                android:supportsRtl="true"
                android:theme="@style/AppTheme">
                //接入友盟
                <meta-data
                    android:name="UMENG_CHANNEL"
                    android:value="${CHANNEL_ID}" />
                <activity android:name=".MainActivity">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
        
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        </manifest>
接着在app层级下的build.gradle申明接入友盟的渠道。

          //通用渠道映射项 def 表示申明
         def flavorHash = [UMENG_APPKEY: UMENG_APPKEY];//UMENG_APPKEY友盟的Appkey，同样写在你新建的gradle.properties文件中
         //渠道分析
         //productFlavors,不同定制的产品
            productFlavors {
        
                //开发阶段
                manychannel_debug {       
                  //冒号前边的是清单文件里设置的键，后边的是值，CHANNEL_DEBUG同样写在你新建的gradle.properties文件中，下面渠道的也一样
                    manifestPlaceholders = [CHANNEL_ID: CHANNEL_DEBUG,
                                            app_icon  : "@mipmap/ic_launcher",
                                            app_name  : "测试版本"]
                }
                //内部测试阶段
                manychannel_firim {
                    manifestPlaceholders = [CHANNEL_ID: CHANNEL_FIRIM,
                                            app_icon  : "@mipmap/ic_money",
                                            app_name  : "fir版本"]
                }
                //豌豆夹
                manychannel_wandoujia {
                    manifestPlaceholders = [CHANNEL_ID: CHANNEL_WANDOUJIA,
                                            app_icon  : "@mipmap/ic_pay",
                                            app_name  : "豌豆荚版本"]
                }
                //360市场
                manychannel_sanliuling {
                    manifestPlaceholders = [CHANNEL_ID: CHANNEL_SANLIULING,
                                            app_icon  : "@mipmap/ic_wechat",
                                            app_name  : "360版本"]
                }
        
            }
[productFlavors背后的延伸](http://blog.csdn.net/oyangyujun/article/details/47071151)
这样简单的多渠道就出来了，如图：
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/step2.png?raw=true)
#### 多渠道步骤二：不同的应用的包名
做完步骤二你会发现并不能把四个包装到同一个手机上，那是你没有对渠道进行分析，配置，下面来分析渠道。
        
        
        //映射渠道分析
        // productFlavors.all{}表示遍历上面定义的渠道，flavor ->flavor.manifestPlaceholders.putAll(flavorHash)（manifestPlaceholders 是上面定义的名字）这表示
        //把flavorHash集合里面的值加到flavor里面在返还回去（Groovy语法），就等于抽出上面productFlavors的共性，其实app_icon，app_name也可以抽到flavorHash中.
        
        productFlavors.all {
                //遍历
            flavor ->
                flavor.manifestPlaceholders.putAll(flavorHash);
                //println flavor
                if (flavor.name.equals("manychannel_debug")) {
                    flavor.versionCode = Integer.valueOf(VERSIONCODE_DEV);//设置版本号
                    flavor.versionName = VERSIONNAME_DEV;//设置版本名，同样写在你新建的gradle.properties文件中
                    flavor.applicationId = "com.example.huangcl.manychannel_debug"//设置包名
    
                } else if (flavor.name.equals("manychannel_firim")) {
                    flavor.versionCode = Integer.valueOf(VERSIONCODE);
                    flavor.versionName = VERSIONNAME;
                    flavor.applicationId = "com.example.huangcl.manychannel_fir"
    
                } else if (flavor.name.equals("manychannel_wandoujia")) {
                    flavor.versionCode = Integer.valueOf(VERSIONCODE);
                    flavor.versionName = VERSIONNAME;
                    flavor.applicationId = "com.example.huangcl.manychannel_wandoujia"
    
                } else if (flavor.name.equals("manychannel_sanliuling")) {
                    flavor.versionCode = Integer.valueOf(VERSIONCODE);
                    flavor.versionName = VERSIONNAME;
                    flavor.applicationId = "com.example.huangcl.manychannel_sanliuling"
                }
        }
#### 多渠道步骤三：不同的应用显示不同的内容
这块就得在代码里面实现了，思路很简单，就是取道清单配置文件中的meta-data的值，注意是application层级的，取到之后判断不同的值，不同的操作就行了。代码：

        public class MainActivity extends AppCompatActivity {
        
            TextView infoTxt;
        
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                infoTxt = (TextView) findViewById(R.id.infoTxt);
        
        
                ApplicationInfo appInfo = null;
                try {
                    appInfo = this.getPackageManager()
                            .getApplicationInfo(getPackageName(),
                                    PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String msg=appInfo.metaData.getString("UMENG_CHANNEL");
                Log.i("chun", msg);
                //println flavor
                if ("debug".equals(msg)) {
                    infoTxt.setText("manychannel_debug");
                } else if ("fir.im".equals(msg)) {
                    infoTxt.setText("manychannel_firim");
                } else if ("zhushou.360.cn".equals(msg)) {
                    infoTxt.setText("manychannel_sanliuling");
                }else if ("wandoujia.com".equals(msg)) {
                    infoTxt.setText("manychannel_wandoujia");
                }
            }
        }
#### 其它
打包的时候，需要签名配置信息，密码等等的，你可以把.jks文件放到app层级下面。如图：
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/step3.png?raw=true)
不多说了，下面贴我完整的build.gradle文件和新建的gradle.properties文件内容。

* build.gradle

            apply plugin: 'com.android.application'
            
            android {
                compileSdkVersion rootProject.ext.compileSdkVersion
                buildToolsVersion rootProject.ext.buildToolsVersion
            
            
            
                defaultConfig {
                    applicationId "com.example.huangcl.manychannel"
                    minSdkVersion rootProject.ext.minSdkVersion
                    targetSdkVersion rootProject.ext.targetSdkVersion
                    versionCode Integer.valueOf(VERSIONCODE)//gradle.properties文件中
                    versionName VERSIONNAME
                    testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
                }
            
                //声明签名信息
                signingConfigs {
                    release {
                        keyAlias KEYALIAS//gradle.properties文件中
                        keyPassword KEYPASSWORD//gradle.properties文件中
                        storeFile file(STOREFILE)//gradle.properties文件中
                        storePassword STOREPASSWORD//gradle.properties文件中
                    }
                }
            
            
                buildTypes {
                    //发行版
                    release {
                        minifyEnabled true
                        //gradle.properties文件中
                        proguardFiles getDefaultProguardFile(PROGUARD_ANDROID), PROGUARD_RULES
                        //签名信息
                        signingConfig signingConfigs.release
                    }
                    //debug版
                    debug {
                        minifyEnabled false
                        proguardFiles getDefaultProguardFile(PROGUARD_ANDROID), PROGUARD_RULES
                        //签名信息
                        signingConfig signingConfigs.release
                    }
            
                }
            
                //渠道分析
                productFlavors {
            
                    //开发阶段
                    manychannel_debug {
                        manifestPlaceholders = [CHANNEL_ID: CHANNEL_DEBUG,
                                                app_icon  : "@mipmap/ic_launcher",
                                                app_name  : "测试版本"]
                    }
                    //内部测试阶段
                    manychannel_firim {
                        manifestPlaceholders = [CHANNEL_ID: CHANNEL_FIRIM,
                                                app_icon  : "@mipmap/ic_money",
                                                app_name  : "fir版本"]
                    }
                    //豌豆夹
                    manychannel_wandoujia {
                        manifestPlaceholders = [CHANNEL_ID: CHANNEL_WANDOUJIA,
                                                app_icon  : "@mipmap/ic_pay",
                                                app_name  : "豌豆荚版本"]
                    }
                    //360市场
                    manychannel_sanliuling {
                        manifestPlaceholders = [CHANNEL_ID: CHANNEL_SANLIULING,
                                                app_icon  : "@mipmap/ic_wechat",
                                                app_name  : "360版本"]
                    }
            
                }
                //通用渠道映射项
                def flavorHash = [UMENG_APPKEY: UMENG_APPKEY];
            
                //映射渠道分析
                productFlavors.all {
                        //
                    flavor ->
                        flavor.manifestPlaceholders.putAll(flavorHash);
                        //println flavor
                        if (flavor.name.equals("manychannel_debug")) {
                            flavor.versionCode = Integer.valueOf(VERSIONCODE_DEV);
                            flavor.versionName = VERSIONNAME_DEV;
                            flavor.applicationId = "com.example.huangcl.manychannel_debug"
            
                        } else if (flavor.name.equals("manychannel_firim")) {
                            flavor.versionCode = Integer.valueOf(VERSIONCODE);
                            flavor.versionName = VERSIONNAME;
                            flavor.applicationId = "com.example.huangcl.manychannel_fir"
            
                        } else if (flavor.name.equals("manychannel_wandoujia")) {
                            flavor.versionCode = Integer.valueOf(VERSIONCODE);
                            flavor.versionName = VERSIONNAME;
                            flavor.applicationId = "com.example.huangcl.manychannel_wandoujia"
            
                        } else if (flavor.name.equals("manychannel_sanliuling")) {
                            flavor.versionCode = Integer.valueOf(VERSIONCODE);
                            flavor.versionName = VERSIONNAME;
                            flavor.applicationId = "com.example.huangcl.manychannel_sanliuling"
                        }
                }
            
            }
            
            dependencies {
                compile fileTree(dir: 'libs', include: ['*.jar'])
                androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
                    exclude group: 'com.android.support', module: 'support-annotations'
                })
                compile "com.android.support:appcompat-v7:$rootProject.ext.supportLibraryVersion"
                testCompile 'junit:junit:4.12'
            }
* gradle.properties
        
            //混淆信息
            PROGUARD_ANDROID=proguard-android.txt
            PROGUARD_RULES=proguard-rules.pro
            //版本信息
            VERSIONCODE=1
            VERSIONNAME=1.0.0
            VERSIONCODE_DEV=2
            VERSIONNAME_DEV=1.1.0
            
            //友盟(值乱填的)
            UMENG_APPKEY=fdsgdffdfhsjfhdsifbdhjsf
            
            //渠道信息
            CHANNEL_DEBUG=debug
            CHANNEL_DEBUG_NAME="测试版本"
            CHANNEL_FIRIM=fir.im
            CHANNEL_FIRIM_NAME="fir版本"
            CHANNEL_WANDOUJIA=wandoujia.com
            CHANNEL_WANDOUJIA_NAME="豌豆荚版本"
            CHANNEL_SANLIULING=zhushou.360.cn
            CHANNEL_SANLIULING_NAME="360版本"
            
            
            //签名配置信息（都不是秘密）
            KEYALIAS=hcl
            KEYPASSWORD=hs19931010
            STOREFILE=./hcl.jks
            STOREPASSWORD=hs19931010
#### 结果
* 同一手机
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/result_1.png?raw=true)
* 不同显示
测试版
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/result_2.jpg.png?raw=true)
fir版
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/result_3.jpg.png?raw=true)
豌豆荚版
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/result_4.jpg.png?raw=true)
360版
![](https://github.com/mar-sir/Manychannels/blob/master/Manychannel/app/src/main/java/images/result_5.jpg.png?raw=true)

[源码地址](https://github.com/mar-sir/Manychannels.git)
