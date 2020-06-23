package cn.udesk.photoselect;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.adapter.PreviewPhotosFragmentAdapter;

/**
 * Created by user on 2018/3/9.
 */

public class PreviewFragment extends Fragment implements PreviewPhotosFragmentAdapter.OnClickListener {

    private OnPreviewFragmentClickListener mListener;

    PreviewPhotosFragmentAdapter adapter;
    private RecyclerView rvPhotos;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.udesk_fragment_preview, container, false);
        try {
            rvPhotos = (RecyclerView) rootView.findViewById(R.id.rv_preview_selected_photos);
            adapter = new PreviewPhotosFragmentAdapter(getActivity().getApplicationContext(), this);
            rvPhotos.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            rvPhotos.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPreviewFragmentClickListener) {
            mListener = (OnPreviewFragmentClickListener) context;
        }
    }

    @Override
    public void onPhotoClick(int position) {
        mListener.onPreviewPhotoClick(position);
    }

    public interface OnPreviewFragmentClickListener {
        void onPreviewPhotoClick(int position);
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        adapter.setChecked(position);
        if (position != -1) {
            rvPhotos.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
