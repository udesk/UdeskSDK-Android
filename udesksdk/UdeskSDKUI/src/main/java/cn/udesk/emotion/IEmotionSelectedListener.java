package cn.udesk.emotion;

public interface IEmotionSelectedListener {
    void onEmojiSelected(String key);

    void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath);
}
