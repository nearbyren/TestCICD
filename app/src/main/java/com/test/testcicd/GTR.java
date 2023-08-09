//package com.test.testcicd;
//
//import static android.app.PendingIntent.getActivity;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.os.Build;
//import android.view.View;
//import android.widget.RemoteViews;
//
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Map;
//
//class GTR {
//    /**
//     * 对 Object 的增删查改操作
//     */
//    public static class SaveObject {
//
//        public static interface SaveBean extends Serializable {
//        }
//
//        /**
//         * 保存Object
//         *
//         * @param context
//         * @param obj
//         * @return 是否保存成功
//         */
//        public static synchronized boolean saveObject(Context context, Object obj) {
//            if (context == null || obj == null) {
//                return false;
//            }
//            try {
//                File file = new File(context.getFilesDir().getPath() + obj.getClass().getName());
//                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
//                out.writeObject(obj);//存储Object
//                out.close();//关闭存储流
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//
//        /**
//         * 删除Object
//         *
//         * @param context
//         * @param cla
//         * @return
//         */
//        public static synchronized boolean deleteObject(Context context, Class<?> cla) {
//            if (context == null || cla == null) {
//                return false;
//            }
//            try {
//                File file = new File(context.getFilesDir().getPath() + cla.getName());
//                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
//                Object obj = new Object();//保证不会空指针
//                try {
//                    obj = cla.newInstance();//实体化
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                    return false;
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                    return false;
//                }
//                out.writeObject(obj);//存储Object
//                out.close();//关闭存储流
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//
//        /**
//         * 获取Object
//         *
//         * @param context
//         * @param cla
//         * @param <T>
//         * @return
//         */
//        public static synchronized <T> T queryObject(Context context, Class<T> cla) {
//
//            if (context == null || cla == null) {
//                return null;
//            }
//            T t = null;
//            try {
//                File file = new File(context.getFilesDir().getPath() + cla.getName());
//                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
//                t = (T) in.readObject();
//                in.close();//关闭反序列化数据流
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            return t;
//
//        }
//
//        /**
//         * 更改Object
//         *
//         * @param context
//         * @param obj
//         * @return
//         */
//        public static synchronized boolean updateObject(Context context, Object obj) {
//            if (context == null || obj == null) {
//                return false;
//            }
//            try {
//                File file = new File(context.getFilesDir().getPath() + obj.getClass().getName());
//                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
//                out.writeObject(obj);//存储Object
//                out.close();//关闭存储流
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//
//    }
//
//    /**
//     * 封装 Notification 通知类
//     */
//    public static class GT_Notification implements SaveObject.SaveBean {
//
//        private static int NOTIFYID = 0x1079; //通知id
//        private static String CHANEL_ID = "com.gsls.king";
//        private static String CHANEL_NAME = "GT_Android";
//        private static String CHANEL_DESCRIPTION = "GT_Description";
//        private static NotificationManagerCompat notificationManagerCompat;
//        public static final String SEPARATOR = "_GT_";//分隔符
//        private static final Map<Integer, Object> map = new HashMap<>();
//
//        public static NotificationManagerCompat getNotificationManagerCompat(Context... contexts) {
////        Context context = getActivity();
//            Context context = null;
//            if (contexts.length > 0) {
//                context = contexts[0];
//            }
//            if (notificationManagerCompat == null) {
//                notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
//            }
//            return notificationManagerCompat;
//        }
//
//        public static void setNotificationManagerCompat(NotificationManagerCompat notificationManagerCompat) {
//            GT_Notification.notificationManagerCompat = notificationManagerCompat;
//        }
//
//        /**
//         * 封装的第一代 通知 (非服务开启，想在服务里启动的话自己加)
//         * <uses-permission android:name="android.permission.POST_NOTIFICATIONS" /><!-- 通知栏权限 -->
//         *
//         * @return
//         */
//        public static abstract class NotificationBase implements SaveObject.SaveBean/*, GT.Frame.ViewModelFeedback */ {
////
////        @Override
////        public void onViewModeFeedback(Object... obj) {
////
////        }
////
////        @Override
////        public ViewModelStore getViewModelStore() {
////            return new ViewModelStore();
////        }
//
//            private Object tag;
//            protected int layout1;
//            protected int layout2;
//            public Context context;
//            public RemoteViews remoteViews1;
//            public RemoteViews remoteViews2;
//            private NotificationCompat.Builder builder;
//            private boolean isShowFrontDesk = false;
//            public Map<Integer, PendingIntent> mapClick = new HashMap<>();//用于记录 单击事件
//            public Map<Integer, String> mapUIText = new HashMap<>();//用于记录 所有当前最新UI
//            public Map<Integer, Object> mapUIImage = new HashMap<>();//用于记录 所有当前最新UI
//            public NotificationManagerCompat notificationManagerCompat;//通知管理器
//            //保存基础信息，用于更新UI
//            private int icon;
//            private boolean clickCancel;
//            private boolean isLockScreenShow;
//            private boolean isOngoing;
//            private String notificationChannel;
//            private Intent intent;
//            private long time;
//            public int NOTIFYID = 0x1079; //通知id
//
//            public Object getTag() {
//                return tag;
//            }
//
//            public void setTag(Object tag) {
//                this.tag = tag;
//            }
//
//            public int getNotifyid() {
//                return NOTIFYID;
//            }
//
//            public void setNotifyid(int notifyid) {
//                NOTIFYID = notifyid;
//            }
//
//            public void setLayout1(int layout1) {
//                this.layout1 = layout1;
//            }
//
//            public void setLayout2(int layout2) {
//                this.layout2 = layout2;
//            }
//
//            public RemoteViews getLayout1() {
//                return remoteViews1;
//            }
//
//            public RemoteViews getLayout2() {
//                return remoteViews2;
//            }
//
//            protected int loadLayout1() {
//                return layout1;
//            }
//
//            protected int loadLayout2() {
//                return layout2;
//            }
//
//            protected View findViewById1(int id) {
//                return null;
//            }
//
//            protected View findViewById2(int id) {
//                return null;
//            }
//
//            protected void bingData() {
//            }
//
//            public NotificationBase() {
//                this.context = /*getActivity();*/null;
//                builder = new NotificationCompat.Builder(context);
//            }
//
//            public NotificationBase(Context context) {
//                this.context = context;
//                builder = new NotificationCompat.Builder(context);
//                bingData();
//                initView(context);
//                loadData(context);
//            }
//
//            /**
//             * 设置属性
//             *
//             * @param icon             图标
//             * @param clickCancel      是否单击取消
//             * @param isLockScreenShow 锁屏是否显示
//             * @param isOngoing        用户是否可以取消（true:不可取消, false:可取消）
//             * @param intent           单击意图
//             * @param time             发送的时间
//             * @param isShowFrontDesk  是否悬浮显示
//             * @param notifyids        通知key
//             */
//            protected NotificationCompat.Builder setInitData(int icon, boolean clickCancel, boolean isLockScreenShow, boolean isOngoing, Intent intent, long time, boolean isShowFrontDesk, int... notifyids) {
//                this.icon = icon;
//                this.clickCancel = clickCancel;
//                this.isLockScreenShow = isLockScreenShow;
//                this.isOngoing = isOngoing;
//                this.intent = intent;
//                this.time = time;
//
//                builder.setAutoCancel(clickCancel);//设置通知打开后自动消失
//
//                //如果 设置了-1，那就默认给透明的图标
//                if (icon <= 0) {
//                    icon = R.color.black;
//                }
//                builder.setSmallIcon(icon);//设置左边的通知图标 且当前属性必须存在
//
//                //设置发送时间
//                if (time > 0) {
//                    if (time == 0) {
//                        time = System.currentTimeMillis();
//                    }
//                    builder.setWhen(time);//设置发送时间
//                }
//
//                builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.FLAG_SHOW_LIGHTS);//设置默认的声音与默认的振动
//                //创建一个启动详细页面的 Intent 对象
//                if (intent == null) {
//                    intent = new Intent();
//                } else {
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                }
//                builder.setContentIntent(PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE));//设置通知栏 点击跳转
//
//
//                PendingIntent pendingIntent = null;
//                // 适配12.0及以上
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//                } else {
//                    pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                }
//                builder.setFullScreenIntent(pendingIntent, true);
//
//                if (notifyids.length > 0) {
//                    NOTIFYID = notifyids[0];
//                }
//
//                builder.setCategory(Notification.CATEGORY_MESSAGE);//设置通知类别
//
//                if (isLockScreenShow)
//                    builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE); // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
//
//                builder.setOngoing(isOngoing);//持久性的通知,用户无法删除
//
//                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
//
//                this.isShowFrontDesk = isShowFrontDesk;
//                if (isShowFrontDesk) {
//                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
//                }
//                return builder;
//            }
//
//            protected void initView(Context context) {
//                if (notificationChannel == null)
//                    notificationChannel = createNotificationChannel(context);
//                builder.setChannelId(notificationChannel);
//                notificationManagerCompat = GT_Notification.getNotificationManagerCompat(context);
//                layout1 = loadLayout1();
//                layout2 = loadLayout2();
//                if (layout1 > 0) {
//                    remoteViews1 = new RemoteViews(context.getPackageName(), layout1);
//                }
//                if (layout2 > 0) {
//                    remoteViews2 = new RemoteViews(context.getPackageName(), layout2);
//                }
//                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
//            }
//
//            public void loadData(Context context) {
//
//                if (remoteViews1 != null) {
//                    builder.setCustomContentView(remoteViews1);
//                }
//                if (remoteViews2 != null) {
//                    builder.setCustomBigContentView(remoteViews2);
//                }
//
//            }
//
//
//            /**
//             * 动态设置修改通知里的图片
//             *
//             * @param imageId  修改图片组件的ID
//             * @param resource 修改图片组件的对象
//             *                 //         * @param isCommit 是否自动提交刷新UI
//             * @return
//             */
//            public NotificationBase setImageRes(int imageId, int resource) {
//                if (imageId < 0 || resource == 0) return this;
//                remoteViews1.setImageViewResource(imageId, resource);
//                remoteViews2.setImageViewResource(imageId, resource);
//                mapUIImage.put(imageId, resource);//保存UI状态
//                return this;
//            }
//
//            private int imageId;
//            private Object resource;
//
//            /**
//             * 动态设置修改通知里的图片
//             *
//             * @param imageId  修改图片组件的ID
//             * @param resource 修改图片组件的对象
//             *                 //         * @param isCommit 是否自动提交刷新UI
//             * @return
//             */
//            public NotificationBase setImageUrl(int imageId, Object resource) {
//                if (imageId < 0 || resource == null) return this;
//                this.imageId = imageId;
//                this.resource = resource;
////            GT.Thread.getInstance(0).execute(new Runnable() {
////                @Override
////                public void run() {
////                    Bitmap bitmap = /*ImageViewTools.ObjectToBitmap(resource);*/null；
////                    mapUIImage.put(imageId, bitmap);//保存UI状态
////                }
////            });
//                return this;
//            }
//
//            /**
//             * 设置文字属性
//             *
//             * @param tvId 文本组件ID
//             * @param text 显示内容
//             *             //         * @param isCommit 是否自动提交刷新UI
//             * @return
//             */
//            public NotificationBase setTextViewText(int tvId, String text) {
//                if (tvId < 0) return this;
//                if (text != null) {
//                    remoteViews1.setTextViewText(tvId, text);
//                    remoteViews2.setTextViewText(tvId, text);
//                    mapUIText.put(tvId, text);//保存UI状态
//                }
//                return this;
//            }
//
//
//            /**
//             * 提交刷新通知
//             *
//             * @param builders
//             * @return
//             */
//            public NotificationBase commit(NotificationCompat.Builder... builders) {
//          /*  if (builders.length > 0) {
//                builder = builders[0];
//            }
//            if (resource != null) {
//                GT.Observable.getDefault().execute(new Observable.RunJavaR<Bitmap>() {
//                    @Override
//                    public Bitmap run() {
//                        return ImageViewTools.ObjectToBitmap(resource);
//                    }
//                }).execute(new Observable.RunAndroidV<Bitmap>() {
//                    @Override
//                    public void run(Bitmap bitmap) {
//                        //重新创建一个通知栏 用于更新UI
//                        if (notificationChannel == null)
//                            notificationChannel = createNotificationChannel(context);
//                        builder = new NotificationCompat.Builder(context, notificationChannel);
//
//                        setInitData(icon, clickCancel, isLockScreenShow, isOngoing, intent, time, isShowFrontDesk, NOTIFYID);
//
//                        //创建新的布局对象 并设更新好UI
//                        remoteViews1 = new RemoteViews(context.getPackageName(), layout1);
//                        remoteViews2 = new RemoteViews(context.getPackageName(), layout2);
//
//                        for (int id : mapUIImage.keySet()) {
//                            Object resObj = mapUIImage.get(id);
//                            if (resObj instanceof Integer) {
//                                int res = Integer.parseInt(String.valueOf(resObj));
//                                remoteViews1.setImageViewResource(id, res);
//                                remoteViews2.setImageViewResource(id, res);
//                            } else if (resObj instanceof Bitmap) {
//                                Bitmap resBit = (Bitmap) resObj;
//                                remoteViews1.setImageViewBitmap(id, resBit);
//                                remoteViews2.setImageViewBitmap(id, resBit);
//                            }
//
//                        }
//
//                        for (int id : mapUIText.keySet()) {
//                            String text = mapUIText.get(id);
//                            remoteViews1.setTextViewText(id, text);
//                            remoteViews2.setTextViewText(id, text);
//                        }
//
//                        remoteViews1.setImageViewBitmap(imageId, bitmap);
//                        remoteViews2.setImageViewBitmap(imageId, bitmap);
//
//                        //将新创建的UI 注入已存在的单击事件
//                        for (int key : mapClick.keySet()) {
//                            PendingIntent pendingIntent = mapClick.get(key);
//                            remoteViews1.setOnClickPendingIntent(key, pendingIntent);
//                            remoteViews2.setOnClickPendingIntent(key, pendingIntent);
//                        }
//
//                        //将布局UI添加通知栏里去
//                        builder.setCustomContentView(remoteViews1);
//                        builder.setCustomBigContentView(remoteViews2);
//                        resource = 0;
//                        imageId = 0;
//
//                        GT_Notification.startNotification(builder, NOTIFYID); //最终发布更新通知栏
//                        resource = null;
//                    }
//                });
//            } else {
//                GT_Notification.startNotification(builder, NOTIFYID); //最终发布更新通知栏
//            }*/
//                return this;
//            }
//
//            public void finish() {
//                onDestroy();
//
//            }
//
//            /**
//             * 清空当前通知
//             *
//             * @param notifyids
//             */
//            public void cancel(int... notifyids) {
//                if (notificationManagerCompat == null) return;
//                if (notifyids.length > 0) {
//                    notificationManagerCompat.cancel(notifyids[0]);//清除当前 id 为 NOTIFYID 的通知
//                } else {
//                    notificationManagerCompat.cancel(NOTIFYID);//清除当前 id 为 NOTIFYID 的通知
//                }
//            }
//
//
//            protected void onDestroy() {
//                if (notificationManagerCompat != null)
//                    notificationManagerCompat.cancel(NOTIFYID);//清除当前 id 为 NOTIFYID 的通知
//                Runtime.getRuntime().gc();
//            }
//
//
//        }
//
//        //封装的第二代 通知 (非服务端开启，想在服务里启动的话自己加)
//        public static abstract class AnnotationNotification extends NotificationBase {
//
//            public AnnotationNotification() {
//                super();
//                registerNotificationReceiver(context);
//            }
//
//            public AnnotationNotification(Context context) {
//                super(context);
//                registerNotificationReceiver(context);
//            }
//
//            private int[] clickViews;
//
//            //注册通知广播
//            private void registerNotificationReceiver(Context context) {
//                //注册广播
//                IntentFilter filter = new IntentFilter();
//                if (clickViews == null || clickViews.length == 0) return;
//                //设置单击事件
//                for (int i = 0; i < clickViews.length; i++) {
//                    filter.addAction(getClass().getName() + ":" + clickViews[i]);
//                }
//                //注册广播
//                context.registerReceiver(mBroadcastReceiver, filter);
//            }
//
//
//            @Override
//            protected void bingData() {
//                super.bingData();
////            GT.build(this);
//            }
//
//            @Override
//            public void setLayout1(int resLayout) {
//                super.setLayout1(resLayout);
//            }
//
//            @Override
//            public void setLayout2(int resLayout) {
//                super.setLayout2(resLayout);
//            }
//
//            @Override
//            protected void initView(Context context) {
//                super.initView(context);
////            EventBus.getDefault().register(this);//注册订阅者
//                setOnClickListener();
//            }
//
//            @Override
//            public NotificationBase commit(NotificationCompat.Builder... builders) {
//                super.commit(builders);
//                return this;
//            }
//
//            //设置单击事件
//            private void setOnClickListener() {
//                Class<?> aClass = getClass();
//                Method[] methods;
//                try {
//                    methods = aClass.getDeclaredMethods();
//                } catch (Throwable th) {
//                    methods = aClass.getMethods();
//                }
//                for (Method method : methods) {
//                    method.setAccessible(true);
////                Annotations.GT_Click initView = method.getAnnotation(Annotations.GT_Click.class);
////                if (initView != null) {
////                    clickViews = initView.value();
//                    if (clickViews == null || clickViews.length == 0) return;
//                    //设置单击事件
//                    for (int i = 0; i < clickViews.length; i++) {
//                        int clickView = clickViews[i];
//                        Intent intent = new Intent(getClass().getName() + ":" + clickView);
//                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//                        mapClick.put(clickView, pendingIntent);
//                        remoteViews1.setOnClickPendingIntent(clickView, pendingIntent);
//                        remoteViews2.setOnClickPendingIntent(clickView, pendingIntent);
//                    }
////                }
//                }
//            }
//
//            // 声明一个广播用于接受单击状态栏
//            public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//                private View view;
//
//                @Override
//                public void onReceive(Context context, Intent intent) {
////                    logt("接收到广播:" + intent.getAction());
//                    onClickView(view, intent);
//                }
//            };
//
//            private void onClickView(View view, Intent intent) {
//                if (view == null) view = new View(context);
//                String action = intent.getAction();
//                if (!action.contains(":")) return;
//                String[] split = action.split(":");
//                if (getClass().getName().equals(split[0])) {//过滤掉不是当前类注册的组件单击事件
//                    try {
//                        view.setId(Integer.parseInt(split[1]));
//                        onClick(view);
//                    } catch (Exception e) {
//
//                    }
//                }
//            }
//
//            //单击事件触发的方法
//            public void onClick(View view) {
//
//            }
//
//            public void finish() {
//                super.finish();
//            }
//
//            @Override
//            protected void onDestroy() {
//                super.onDestroy();
////            EventBus.getDefault().unregister(this);//取消订阅者
//                try {
//                    context.unregisterReceiver(mBroadcastReceiver);
//                } catch (Exception e) {
//
//                }
//            }
//
//            /**
//             * 内容由 ViewModel 层去提供数据 给到 View 层
//             *
//             * @param obj
//             */
//            public void onViewModeFeedback(Object... obj) {
//
//            }
//
//        }
//
//        //封装的第三代 通知 (非服务端开启，想在服务里启动的话自己加)
//        public abstract static class DataBindingNotification extends AnnotationNotification {
//
//            public DataBindingNotification() {
//                super();
//            }
//
//            public DataBindingNotification(Context context) {
//                super(context);
//            }
//
//
//        }
//
//        /**
//         * @param context          上下文
//         * @param icon             左上角图片
//         * @param iconBottomRight  右下角图片
//         * @param title            标题
//         * @param msg              通知内容
//         * @param clickCancel      点击是否取消
//         * @param isShowFrontDesk  是否在前台(浮动)显示
//         * @param isLockScreenShow 是否锁屏显示
//         * @param intent           点击意图
//         * @param time             发送通知的时间 0：使用当前系统时间 -1：不显示发送时间 >0:自定义时间
//         * @param progressMax      进度最大值
//         * @param progressCurrent  进度当前值
//         * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationProgress(Context context, int icon, Object iconBottomRight, String title, String msg, boolean clickCancel, boolean isShowFrontDesk, boolean isLockScreenShow, Intent intent, long time, int progressMax, int progressCurrent, int... notifyids) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, iconBottomRight, title, msg, clickCancel, isLockScreenShow, intent, time, false, notifyids);
//
//            if (isShowFrontDesk) {
//                notification.setTicker(msg);
//                PendingIntent pendingIntent = null;
//
//                // 适配12.0及以上
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//                } else {
//                    pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                }
//                notification.setFullScreenIntent(pendingIntent, true);
//
//                notification.setDefaults(NotificationCompat.DEFAULT_ALL);
//                notification.setPriority(Notification.PRIORITY_HIGH);
//
//            }
//
//            notification.setOngoing(true);//持久性的通知,用户无法删除
//            notification.setProgress(progressMax, progressCurrent, false);
//            return notification;
//        }
//
//        /**
//         * 更新度条通知进
//         *
//         * @param builder         进度条通知对象
//         * @param msg             下载消息
//         * @param progressMax     进度条最大值
//         * @param progressCurrent 当前进度
//         * @param isHide          是否隐藏到后台进行下载
//         * @param endAutoCancel   进度条到达最大值后是否自动取消通知
//         * @param endMsg          进度条到达最大值后的 信息
//         * @param sleepTime       自动取消通知的延时时间(若不设置自动取消则该参数无效)
//         * @param notifyids       通知 key
//         */
//        public static void updateNotificationProgress(NotificationCompat.Builder builder, String msg, int progressMax, int progressCurrent, boolean isHide, boolean endAutoCancel, String endMsg, int sleepTime, int... notifyids) {
//            if (notifyids.length > 0) {
//                NOTIFYID = notifyids[0];
//            }
//
//            if (sleepTime <= 0) sleepTime = 3000;
//
//            //刷新UI
//            if (msg != null)
//                builder.setContentText(msg);
//            builder.setProgress(progressMax, progressCurrent, false);
//
//            //是否隐藏
//            if (isHide/* && builder.getPriority() != NotificationCompat.PRIORITY_DEFAULT*/) {
////            Thread.runJava(() -> {
////                Thread.sleep(1000);
//                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
////            });
//            }
//
//            //结束关闭
//            if (progressCurrent >= progressMax) {
//                if (endMsg != null)
//                    builder.setContentText(endMsg);
//                notificationManagerCompat.notify(NOTIFYID, builder.build());
//                if (endAutoCancel) {
////                Thread.sleep(sleepTime);
//                    GT_Notification.cancelNotification(NOTIFYID);
//                }
//            } else {
//                notificationManagerCompat.notify(NOTIFYID, builder.build());
//            }
//        }
//
//        /**
//         * @param context          上下文
//         * @param icon             左上角图片
//         * @param iconBottomRight  右下角图片
//         * @param iconMax          大图片
//         * @param title            标题
//         * @param msg              通知内容
//         * @param clickCancel      点击是否取消
//         * @param isLockScreenShow 锁屏是否显示
//         * @param intent           点击意图
//         * @param time             发送通知的时间 0：使用当前系统时间 -1：不显示发送时间 >0:自定义时间
//         * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationImg(Context context, int icon, Object iconBottomRight, int iconMax, String title, String msg, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, int... notifyids) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, iconBottomRight, title, msg, clickCancel, isLockScreenShow, intent, time, false, notifyids);
//            if (iconMax > 0)
//                notification.setStyle(new NotificationCompat.BigPictureStyle()
//                        .bigPicture(BitmapFactory.decodeResource(context.getResources(), iconMax)));
//            return notification;
//        }
//
//        /**
//         * @param context          上下文
//         * @param icon             左上角图片
//         * @param iconBottomRight  右下角图片
//         * @param text             主要内容
//         * @param title            标题
//         *                         //     * @param msg              通知内容
//         * @param clickCancel      点击是否取消
//         * @param isLockScreenShow 锁屏是否显示
//         * @param intent           点击意图
//         * @param time             发送通知的时间 0：使用当前系统时间 -1：不显示发送时间 >0:自定义时间
//         * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationText(Context context, int icon, Object iconBottomRight, String title, String text, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, int... notifyids) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, iconBottomRight, title, null, clickCancel, isLockScreenShow, intent, time, false, notifyids);
//            if (text != null) {
//                notification.setStyle(
//                        new NotificationCompat.BigTextStyle().bigText(text)
//                );
//            }
//            return notification;
//        }
//
//        public static NotificationCompat.Builder createNotificationMsgs(Context context, int icon, Object iconBottomRight, String title, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, int notifyid, String... msgs) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, iconBottomRight, title, null, clickCancel, isLockScreenShow, intent, time, false, notifyid);
//            if (msgs.length > 0) {
//                NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Msg");
//                for (String text : msgs) {
//                    String[] split = text.split(SEPARATOR);
//                    switch (split.length) {
//                        case 1:
//                            messagingStyle.addMessage(text, System.currentTimeMillis(), "");
//                            break;
//                        case 2:
//                            messagingStyle.addMessage(split[1], System.currentTimeMillis(), split[0]);
//                            break;
//                        case 3:
//                            messagingStyle.addMessage(split[1], Long.parseLong(split[2]), split[0]);
//                            break;
//                    }
//                }
//                notification.setStyle(messagingStyle);
//                map.put(notifyid, messagingStyle);
//            }
//            return notification;
//        }
//
//        /**
//         * 添加一条新的 消息
//         *
//         * @param builder  通知对象
//         * @param notifyid 通知 key
//         *                 //     * @param texts    新增加的消息
//         * @return
//         */
//        public static NotificationCompat.Builder addLineMsg(NotificationCompat.Builder builder, int notifyid, String... msgs) {
//            if (msgs.length > 0) {
//                NotificationCompat.MessagingStyle messagingStyle = (NotificationCompat.MessagingStyle) map.get(notifyid);
//                if (messagingStyle != null)
//                    for (String text : msgs) {
//                        String[] split = text.split(SEPARATOR);
//                        switch (split.length) {
//                            case 1:
//                                messagingStyle.addMessage(text, System.currentTimeMillis(), "");
//                                break;
//                            case 2:
//                                messagingStyle.addMessage(split[1], System.currentTimeMillis(), split[0]);
//                                break;
//                            case 3:
//                                messagingStyle.addMessage(split[1], Long.parseLong(split[2]), split[0]);
//                                break;
//                        }
//                    }
//                builder.setStyle(messagingStyle);
//            }
//            NOTIFYID = notifyid;
//            notificationManagerCompat.notify(NOTIFYID, builder.build());
//            return builder;
//        }
//
//        /**
//         * @param context          上下文
//         * @param icon             左上角图片
//         * @param iconBottomRight  右下角图片
//         *                         //     * @param text             主要内容
//         * @param title            标题
//         *                         //     * @param msg              通知内容
//         * @param clickCancel      点击是否取消
//         * @param isLockScreenShow 锁屏是否显示
//         * @param intent           点击意图
//         * @param time             发送通知的时间 0：使用当前系统时间 -1：不显示发送时间 >0:自定义时间
//         *                         //     * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationTexts(Context context, int icon, Object iconBottomRight, String title, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, int notifyid, String... texts) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, iconBottomRight, title, null, clickCancel, isLockScreenShow, intent, time, false, notifyid);
//            if (texts.length > 0) {
//                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//                for (String text : texts) {
//                    inboxStyle.addLine(text);
//                }
//                notification.setStyle(inboxStyle);
//                map.put(notifyid, inboxStyle);
//            }
//            return notification;
//        }
//
//        /**
//         * 添加一条新的 数据
//         *
//         * @param builder  通知对象
//         * @param notifyid 通知 key
//         * @param texts    新增加的数据
//         * @return
//         */
//        public static NotificationCompat.Builder addLineText(NotificationCompat.Builder builder, int notifyid, String... texts) {
//            if (texts.length > 0) {
//                NotificationCompat.InboxStyle inboxStyle = (NotificationCompat.InboxStyle) map.get(notifyid);
//                if (inboxStyle != null)
//                    for (String text : texts) {
//                        inboxStyle.addLine(text);
//                    }
//                builder.setStyle(inboxStyle);
//            }
//            NOTIFYID = notifyid;
//            notificationManagerCompat.notify(NOTIFYID, builder.build());
//            return builder;
//        }
//
//        /**
//         * 创建简易的 通知
//         *
//         * @param context          上下文
//         * @param icon             图标
//         * @param layout1          折叠布局
//         * @param layout2          展开布局
//         * @param clickCancel      单击是否取消通知
//         * @param isLockScreenShow 锁屏的时候是否显示
//         * @param intent           单击跳转意图
//         * @param time             发送通知时间
//         * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationFoldView(Context context, int icon, int layout1, int layout2, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, int... notifyids) {
//            NotificationCompat.Builder notification = createNotificationForNormal(context, icon, -1, null, null, clickCancel, isLockScreenShow, intent, time, false, notifyids);
//            RemoteViews layoutView1 = null, layoutView2 = null;
//            if (layout1 > 0) {
//                layoutView1 = new RemoteViews(context.getPackageName(), layout1);
//            }
//            if (layout2 > 0) {
//                layoutView2 = new RemoteViews(context.getPackageName(), layout2);
//            }
//            notification.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
//
//            if (layoutView2 != null) {
//                notification.setCustomBigContentView(layoutView2);
//            }
//            if (layoutView1 != null) {
//                notification.setCustomContentView(layoutView1);
//            }
//            return notification;
//        }
//
//        /**
//         * 创建普通通知
//         *
//         * @param context          上下文
//         * @param icon             左上角图片
//         * @param iconBottomRight  右下角图片
//         * @param title            标题
//         * @param msg              通知内容
//         * @param clickCancel      点击是否取消
//         * @param isLockScreenShow 锁屏是否显示
//         * @param intent           点击意图
//         * @param time             发送通知的时间 0：使用当前系统时间 -1：不显示发送时间 >0:自定义时间
//         * @param isStart          是否直接启动通知
//         * @param notifyids        通知 key
//         * @return
//         */
//        public static NotificationCompat.Builder createNotificationForNormal(Context context, int icon, Object iconBottomRight, String title, String msg, boolean clickCancel, boolean isLockScreenShow, Intent intent, long time, boolean isStart, int... notifyids) {
//
//            if (notificationManagerCompat == null) {
//                notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
//            }
//
//            Bitmap bitmap = null;
//            if (iconBottomRight instanceof Integer) {
//                bitmap = BitmapFactory.decodeResource(/*getActivity().getResources()*/null, (Integer) iconBottomRight);
//            } else if (iconBottomRight instanceof Bitmap) {
//                bitmap = (Bitmap) iconBottomRight;
//            }
//
//            String name = createNotificationChannel(context);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, name);//创建Notification并与Channel关联
//            builder.setAutoCancel(clickCancel);//设置通知打开后自动消失
//
//            PendingIntent pendingIntent = null;
//            // 适配12.0及以上
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//            } else {
//                pendingIntent = getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            }
//            builder.setFullScreenIntent(pendingIntent, true);
//
//            //如果 设置了-1，那就默认给透明的图标
//            if (icon <= 0) {
//                icon = R.color.black;
//            }
//
//            builder.setSmallIcon(icon);//设置左边的通知图标 且当前属性必须存在
//            if (bitmap != null) {
//                builder.setLargeIcon(bitmap);//设置右下角的图标 该属性可不设置
//            }
//            if (title != null)
//                builder.setContentTitle(title);//设置标题
//            if (msg != null)
//                builder.setContentText(msg);//设置内容
//
//            //设置发送时间
//            if (time > 0) {
//                if (time == 0) {
//                    time = System.currentTimeMillis();
//                }
//                builder.setWhen(time);//设置发送时间
//            }
//
//            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.FLAG_SHOW_LIGHTS);//设置默认的声音与默认的振动
//            //创建一个启动详细页面的 Intent 对象
//            if (intent == null) {
//                intent = new Intent();
//            } else {
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            }
//            builder.setContentIntent(PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_IMMUTABLE));//设置通知栏 点击跳转
//
//            if (notifyids.length > 0) {
//                NOTIFYID = notifyids[0];
//            }
//
//            if (isLockScreenShow)
//                builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE); // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
//
//            if (notifyids.length > 0) {
//                NOTIFYID = notifyids[0];
//            }
//            if (isStart) {
//                notificationManagerCompat.notify(NOTIFYID, builder.build());
//            }
//
//            return builder;
//
//        }
//
//        /**
//         * 启动通知
//         *
//         * @param builder   启动的通知
//         * @param notifyids 通知 key 若不填默认是最近创建的通知 key
//         */
//        public static void startNotification(NotificationCompat.Builder builder, int... notifyids) {
//            if (notifyids.length > 0) {
//                NOTIFYID = notifyids[0];
//            }
//            notificationManagerCompat.notify(NOTIFYID, builder.build());
//        }
//
//        /**
//         * 启动GT通知
//         *
//         * @param notificationBase
//         */
//        public static NotificationBase startNotification(NotificationBase notificationBase) {
//            notificationManagerCompat.notify(NOTIFYID, notificationBase.builder.build());
//            return notificationBase;
//        }
//
//        /**
//         * 删除通知
//         *
//         * @param notifyids 被删除通知的 key
//         */
//        public static void cancelNotification(int... notifyids) {
//            if (notifyids.length > 0) {
//                NOTIFYID = notifyids[0];
//            }
//            GT_Notification.getNotificationManagerCompat(/*getActivity()*/null).cancel(NOTIFYID);//清除当前 id 为 NOTIFYID 的通知
//        }
//
//        //清空通知
//        public static void emptyNotification() {
//            notificationManagerCompat.cancelAll();
//        }
//
//        /**
//         * 创建 通知名称
//         *
//         * @param context
//         * @return
//         */
//        private static String createNotificationChannel(Context context) {
//            // O (API 26)及以上版本的通知需要NotificationChannels。
//
//            CHANEL_ID = context.getPackageName();
//
//            if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                // 初始化NotificationChannel
//                NotificationChannel notificationChannel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH);
//                notificationChannel.enableLights(true); //是否在桌面icon右上角展示小红点
//                notificationChannel.setLightColor(Color.GREEN); //小红点颜色
//                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
//                notificationChannel.setDescription(CHANEL_DESCRIPTION);
//                // 向系统添加 NotificationChannel。试图创建现有通知
//                // 通道的初始值不执行任何操作，因此可以安全地执行
//                // 启动顺序
//                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.createNotificationChannel(notificationChannel);
//                return CHANEL_ID;
//            } else {
//                return CHANEL_ID; // 为pre-O(26)设备返回 null
//            }
//        }
//
//    }
//
//
//}