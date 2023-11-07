package com.github.tvbox.osc.bbox.update.component;

import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.github.tvbox.osc.bbox.update.utils.HProgressDialogUtils;
import com.github.tvbox.osc.bbox.util.HawkConfig;
import com.github.tvbox.osc.bbox.util.LOG;
import com.orhanobut.hawk.Hawk;
import com.xuexiang.xupdate.entity.PromptEntity;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IUpdatePrompter;
import com.xuexiang.xupdate.proxy.IUpdateProxy;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.io.File;

public class CustomUpdatePrompter implements IUpdatePrompter {
    /**
     * 显示自定义提示
     */
    private void showUpdatePrompt(final @NonNull UpdateEntity updateEntity, final @NonNull IUpdateProxy updateProxy) {
        String updateInfo = UpdateUtils.getDisplayUpdateInfo(updateProxy.getContext(), updateEntity);

        AlertDialog.Builder builder = new AlertDialog.Builder(updateProxy.getContext())
            .setTitle(String.format("检测到新版本！是否下载？\n忽略后不再显示小红点提醒，可手动检查更新", updateEntity.getVersionName()))
            .setMessage(updateInfo)
            .setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateProxy.startDownload(updateEntity, new OnFileDownloadListener() {
                        @Override
                        public void onStart() {
                            LOG.i("onStart()");
                            HProgressDialogUtils.showHorizontalProgressDialog(updateProxy.getContext(), "下载进度", true);
                        }

                        @Override
                        public void onProgress(float progress, long total) {
                            LOG.i("onProgress()");
                            HProgressDialogUtils.setProgress(Math.round(progress * 100));
                            HProgressDialogUtils.setMax(total);
                        }

                        @Override
                        public boolean onCompleted(File file) {
                            LOG.i("onCompleted()");
                            HProgressDialogUtils.cancel();
                            return true;
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            LOG.i("onError()");
                            HProgressDialogUtils.cancel();
                        }
                    });
                }
            });
        if (updateEntity.isIgnorable()) {
            builder.setNegativeButton("忽略更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // UpdateUtils.saveIgnoreVersion(updateProxy.getContext(), updateEntity.getVersionName());
                    Hawk.put(HawkConfig.IS_IGNORE_VERSION, true);
                    LOG.i("已忽略更新...");
                }
            }).setCancelable(true);
        } else  {
            builder.setCancelable(false);
        }
        builder.create().show();
    }

    /**
     * 显示版本更新提示
     *
     * @param updateEntity 更新信息
     * @param updateProxy  更新代理
     * @param promptEntity 提示界面参数
     */
    @Override
    public void showPrompt(@NonNull UpdateEntity updateEntity, @NonNull IUpdateProxy updateProxy, @NonNull PromptEntity promptEntity) {
        showUpdatePrompt(updateEntity, updateProxy);
    }
}