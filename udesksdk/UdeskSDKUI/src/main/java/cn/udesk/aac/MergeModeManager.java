package cn.udesk.aac;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MergeModeManager {
    private static MergeModeManager mInstance=new MergeModeManager();
    private LinkedHashMap<Long,MergeMode>  mergeModeLinkedHashMap;
    private MergeModeManager(){
        mergeModeLinkedHashMap=new LinkedHashMap<>();
        executor=Executors.newSingleThreadExecutor();
    }
    public static MergeModeManager getmInstance(){
        return mInstance;
    }
    private ExecutorService executor;
    private Future<?> future;
    public void putMergeMode(final MergeMode mergeMode, final MutableLiveData liveData){
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

    public void  dealMergeMode(final MergeMode mergeMode, final MutableLiveData liveData) {
        try {
            future=executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!mergeModeLinkedHashMap.isEmpty()) {
                        if (mergeModeLinkedHashMap.containsKey(mergeMode.getId())) {
                            mergeModeLinkedHashMap.remove(mergeMode.getId());
                        }
                        if (!mergeModeLinkedHashMap.isEmpty()){
                            Iterator<Map.Entry<Long, MergeMode>> iterator = mergeModeLinkedHashMap.entrySet().iterator();
                            if (iterator!=null&&iterator.hasNext()) {
                                Map.Entry<Long, MergeMode> next = iterator.next();
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
            mergeModeLinkedHashMap.clear();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
