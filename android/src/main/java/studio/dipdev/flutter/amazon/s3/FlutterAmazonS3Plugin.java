package studio.dipdev.flutter.amazon.s3;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.UnsupportedEncodingException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterAmazonS3Plugin implements FlutterPlugin, MethodCallHandler {
        private AwsHelper awsHelper;
        private Context context;
        private MethodChannel channel;
        private Context mContext;

        public static void registerWith(PluginRegistry.Registrar registrar) {
            final FlutterAmazonS3Plugin instance = new FlutterAmazonS3Plugin();
            instance.whenAttachedToEngine(registrar.context(), registrar.messenger());
        }

        @Override
        public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
            whenAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
        }

        private void whenAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
            this.mContext = applicationContext;
            channel = new MethodChannel(messenger, "flutter_amazon_s3");
            channel.setMethodCallHandler(this);
        }

        @Override
        public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
            mContext = null;
            channel.setMethodCallHandler(null);
            channel = null;
        }

        @Override
        public void onMethodCall(MethodCall call, final Result result) {
            if (call.method.equals("uploadImageToAmazon")) {
                System.out.println("\n uploadToAmazn method channel invoked: ");

                String filePath = call.argument("filePath");
                String bucket = call.argument("bucket");
                String identity = call.argument("identity");
                String key = call.argument("key");
                File file = new File(filePath);

                try {
                    awsHelper = new AwsHelper(mContext, new AwsHelper.OnUploadCompleteListener() {
                        @Override
                        public void onFailed() {
                            System.out.println("\n❌ upload failed");
                            result.success("Failed");
                        }

                        @Override
                        public void onUploadComplete(@NotNull String imageUrl) {
                            System.out.println("\n✅ upload complete: " + imageUrl);
                            result.success(imageUrl);
                        }
                    }, bucket, identity, key);
                    awsHelper.uploadImage(file);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                result.notImplemented();
            }
        }
}