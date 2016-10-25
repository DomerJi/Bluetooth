package com.jsp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class ScrollingActivity extends BaseActivity {

    private Snackbar snackbar;
    private Context mContext;
    private BluetoothAdapter adapter;
    private AnimationSet animset;
    private FloatingActionButton fab;

    private ArrayList<BluetoothDevice> mainList = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private MyAdapter lvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        mContext = this;

        check();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar = Snackbar.make(view, "搜索蓝牙中...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
                snackbar.show();
                if (fab.getAnimation() == null) {
                    startAnimation(fab);
                    openBluetooth();
                }


            }
        });

        ListView lv = (ListView) findViewById(R.id.main_lv);
        lvAdapter = new MyAdapter();
        lv.setAdapter(lvAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.cancelDiscovery();
                stopAnimation(fab);
                connectDevice(mainList.get(position));
            }
        });


    }

    private void check() {
        // 检查设备是否支持蓝牙      
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(mContext, "该设备不支持蓝牙", Toast.LENGTH_LONG).show();
        }

    }

    private void openBluetooth() {
        // 打开蓝牙   
        if (!adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置蓝牙可见性，最多300秒   
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            mContext.startActivity(intent);
        }
// 设置广播信息过滤   
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
// 注册广播接收器，接收并处理搜索结果   
        mContext.registerReceiver(receiver, intentFilter);
// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去   
        adapter.startDiscovery();
    }


    private void startAnimation(View view) {
        animset = new AnimationSet(false);
        RotateAnimation mrotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mrotate.setDuration(800);
        //mrotate.setRepeatMode(repeatMode)

        LinearInterpolator ddd = new LinearInterpolator();
        mrotate.setInterpolator(ddd);
        mrotate.setRepeatCount(10000);
        mrotate.setFillAfter(true);

        //LinearInterpolator lir = new LinearInterpolator();
        //  mrotate.setInterpolator(lir);

        animset.addAnimation(mrotate);
        view.startAnimation(animset);
    }

    private void stopAnimation(View view) {
        view.clearAnimation();
    }


    //自定义广播类

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("BLUE", action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mainList.contains(device)) {
                    mainList.add(device);
                    lvAdapter.notifyDataSetChanged();
                    Log.e("BLUE", device.getName() + "__add");
                }


                Log.e("BLUE", device.getName() + "__");
                // 搜索到的不是已经绑定的蓝牙设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 显示在TextView上

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                stopAnimation(fab);
            } else {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (!mainList.contains(device)) {
                        mainList.add(device);
                        lvAdapter.notifyDataSetChanged();
                        Log.e("BLUE", device.getName() + "__add");
                    }
                }
            }
            Log.e("BLUE", "size = " + mainList.size());

            lvAdapter.notifyDataSetChanged();
        }
    };

    protected void connectDevice(BluetoothDevice mBluetoothDevice) {
        try {
            // 连接建立之前的先配对
            if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                Method creMethod = BluetoothDevice.class
                        .getMethod("createBond");
                Log.e("TAG", "开始配对");
                creMethod.invoke(mBluetoothDevice);
            } else {
                removeBond(BluetoothDevice.class, mBluetoothDevice);
                Log.e("TAG", "配对失败" + mBluetoothDevice.getBondState() + "____" + BluetoothDevice.BOND_NONE);
            }
        } catch (Exception e) {
            // TODO: handle exception
            //DisplayMessage("无法配对！");
            Log.e("TAG", "配对失败");
            e.printStackTrace();
        }
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mainList.size();
        }

        @Override
        public Object getItem(int position) {
            return mainList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_imp, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BluetoothDevice device = mainList.get(position);
            holder.title.setText(device.getName() + getState(device.getBondState()));
            return convertView;
        }
    }

    private String getState(int state) {
        switch (state) {
            case BluetoothDevice.BOND_NONE:
                return "";
            case BluetoothDevice.BOND_BONDING:
                return "\n正在连接";
            case BluetoothDevice.BOND_BONDED:
                return "\n已连接";
            default:
                return "";
        }
    }

    class ViewHolder {
        TextView title;
    }


//    private void tx(){
//        if (isConnect) {
//            try {
//                OutputStream outStream = socket.getOutputStream();
//                outStream.write(getHexBytes(message));
//            } catch (IOException e) {
//                setState(WRITE_FAILED);
//                Log.e("TAG", e.toString());
//            }
//            try {
//                InputStream inputStream = socket.getInputStream();
//                int data;
//                while (true) {
//                    try {
//                        data = inputStream.read();
//                        Message msg = handler.obtainMessage();
//                        msg.what = DATA;
//                        msg.arg1 = data;
//                        handler.sendMessage(msg);
//                    } catch (IOException e) {
//                        setState(READ_FAILED);
//                        Log.e("TAG", e.toString());
//                        break;
//                    }
//                }
//            } catch (IOException e) {
//                setState(WRITE_FAILED);
//                Log.e("TAG", e.toString());
//            }
//        }
//
//        if (socket != null) {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                Log.e("TAG", e.toString());
//            }
//        }
//    }
//}
//}
//    private byte[] getHexBytes(String message) {
//        int len = message.length() / 2;
//        char[] chars = message.toCharArray();
//        String[] hexStr = new String[len];
//        byte[] bytes = new byte[len];
//        for (int i = 0, j = 0; j < len; i += 2, j++) {
//            hexStr[j] = "" + chars[i] + chars[i + 1];
//            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
//        }
//        return bytes;
//    }



}
