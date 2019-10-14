package cn.udesk.aac;

import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MergeModeManager {
    private static MergeModeManager mInstance;
    private volatile Map<String,MergeMode>  mergeModeLinkedHashMap;
    private Future<?> future2;

    private MergeModeManager(){
        mergeModeLinkedHashMap=Collections.synchronizedMap(new LinkedHashMap<String,MergeMode>());
        executor=Executors.newSingleThreadExecutor();
    }
    public static MergeModeManager getmInstance(){
        synchronized(MergeModeManager.class){
            if(mInstance == null){
                mInstance = new MergeModeManager();
            }
        }
        return mInstance;
    }

    private ExecutorService executor;
    private Future<?> future;
    public  void putMergeMode(final MergeMode mergeMode, final MutableLiveData liveData){
        try {
            future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mergeMode == null) {
                        return;
                    }
                    if (!mergeModeLinkedHashMap.isEmpty()) {
                        mergeModeLinkedHashMap.put(mergeMode.getId(), mergeMode);
                        return;
                    }
                    mergeModeLinkedHashMap.put(mergeMode.getId(), mergeMode);
                    liveData.postValue(mergeMode);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void  dealMergeMode(final MergeMode mergeMode, final MutableLiveData liveData) {
        try {
            future2 = executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!mergeModeLinkedHashMap.isEmpty()) {
                        if (mergeModeLinkedHashMap.containsKey(mergeMode.getId())) {
                            mergeModeLinkedHashMap.remove(mergeMode.getId());
                        }
                        if (!mergeModeLinkedHashMap.isEmpty()) {
                            Iterator<Map.Entry<String, MergeMode>> iterator = mergeModeLinkedHashMap.entrySet().iterator();
                            if (iterator != null && iterator.hasNext()) {
                                Map.Entry<String, MergeMode> next = iterator.next();
                                liveData.postValue(next.getValue());
                            }
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void clear() {
        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
            if (future2 != null) {
                future2.cancel(true);
                future2 = null;
            }

            mergeModeLinkedHashMap.clear();
            executor=null;
            mergeModeLinkedHashMap=null;
            mInstance=null;
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
