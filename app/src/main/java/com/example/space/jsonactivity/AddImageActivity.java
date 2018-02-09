package com.example.space.jsonactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

public class AddImageActivity extends AppCompatActivity {

    public static final int FIRST_IMAGE_REQUEST = 20;
    public static final int SECOND_IMAGE_REQUEST = 30;
    public static final int THIRD_IMAGE_REQUEST = 40;

    public static String pathname = Environment.getExternalStorageDirectory() + "/shadow/shadow_matching_scene_data.json";
    public String destinationFolder = "/storage/emulated/0/shadow/";

    public Uri firstImageUri;
    public Uri secondImageUri;
    public Uri thirdImageUri;

    private ImageView imgView1;
    private ImageView imgView2;
    private ImageView imgView3;


    public Map<String, String> map1 = new HashMap<String, String>();
    public Map<String, String> map2 = new HashMap<String, String>();


    public Map<String, String> map3 = new HashMap<String, String>();
    public ArrayList<Map<String, String>> data = new ArrayList<>();
    public ArrayList<ArrayList<Map<String, String>>> sceneData = new ArrayList<>();


    //Functioin to copy selected image to the destination folder
    public static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());
            if (src.isDirectory()) {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Functioin to copy selected image to the destination folder
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        //get a reference to the imageview that holds tha image that the user will see
        imgView1 = (ImageView) findViewById(R.id.imgView1);
        imgView2 = (ImageView) findViewById(R.id.imgView2);
        imgView3 = (ImageView) findViewById(R.id.imgView3);
        //check whether the permission to read from and write to memory is given
        //isStoragePermissionGranted();
        try {
            readFromJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //this method will be invoked when user clicks first imageview to add an image
    public void onFirstImageViewClicked(View v) {
        //invoke image gallery using an implicit intent
        Intent firstImagePickerIntent = new Intent(Intent.ACTION_PICK);
        //where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        //get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);
        // System.out.println(">>>>>>>>"+data);
        //set data and type. Get all image types
        firstImagePickerIntent.setDataAndType(data, "image/*");
        //we will invoke this activity and get something from it
        startActivityForResult(firstImagePickerIntent, FIRST_IMAGE_REQUEST);
    }

    //this method will be invoked when user clicks second imageview to add an image
    public void onSecondImageViewClicked(View v) {
        Intent secondImagePickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        secondImagePickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(secondImagePickerIntent, SECOND_IMAGE_REQUEST);
    }

    //this method will be invoked when user clicks third imageview to add an image
    public void onThirdImageViewClicked(View v) {
        Intent thirdimagePickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        thirdimagePickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(thirdimagePickerIntent, THIRD_IMAGE_REQUEST);
    }

    //this method will be invoked when SUBMIT button is pressed
    public void onButtonClick(View view) {
        //flag indicates whether any of the image feilds are left vacant
        int flag = 0;
        //function calls to copy the selected images to the specified destinaion folder
        try {
            copyFileOrDirectory(getPath(firstImageUri), destinationFolder);
        } catch (Exception e) {
            e.printStackTrace();
            flag = 1;
        }
        try {
            copyFileOrDirectory(getPath(secondImageUri), destinationFolder);
        } catch (Exception e) {
            e.printStackTrace();
            flag = 1;
        }
        try {
            copyFileOrDirectory(getPath(thirdImageUri), destinationFolder);
        } catch (Exception e) {
            e.printStackTrace();
            flag = 1;
        }
        //Toasts when images are not added
        if (flag == 1) {
            Toast.makeText(this, "Add All Images", Toast.LENGTH_LONG).show();
        }
        //if all images are added go back to the main activity displaying a toast
        if (flag == 0) {
            finish();
            try {
                writeToJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, " Images Added Successfully", Toast.LENGTH_LONG).show();
            //outFile();
        }
    }

    //control comes here when gallery is opened
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //if we are here everything processed successfully
            if (requestCode == FIRST_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery

                //Address of the image on the SD card
                firstImageUri = data.getData();

                //declare a stream to read the image from the SD card
                InputStream firstInputStream;
                //we are getting an inputstream based on the Uri of the image
                try {
                    firstInputStream = getContentResolver().openInputStream(firstImageUri);
                    //get a bitmap from the stream
                    Bitmap firstimage = BitmapFactory.decodeStream(firstInputStream);
                    //show tha image to the user
                    imgView1.setImageBitmap(firstimage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //show a message to the user that image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
                // System.out.println("------"+getPath(firstImageUri)+"------");
                //tempsceneSet.add(newImagePathMaker(firstImageUri));
                map1.put("src", newImagePathMaker(firstImageUri));
                map1.put("answer", "1");
                System.out.println("---First----" + map1);
                //newImagePathMaker(firstImageUri);
            }
            if (requestCode == SECOND_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery
                //Address of the image on the SD card
                secondImageUri = data.getData();
                //declare a stream to read the image from the SD card
                InputStream secondInputStream;
                //we are getting an inputstream based on the Uri of the image
                try {
                    secondInputStream = getContentResolver().openInputStream(secondImageUri);
                    //get a bitmap from th stream
                    Bitmap secondimage = BitmapFactory.decodeStream(secondInputStream);
                    //show tha image to the user
                    imgView2.setImageBitmap(secondimage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //show a message to the user that image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                    // System.out.println("------"+getPath(secondImageUri)+"------");

                }
                //tempsceneSet.add(newImagePathMaker(secondImageUri));
                map2.put("src", newImagePathMaker(secondImageUri));
                map2.put("answer", "2");
                System.out.println("---Second----" + map2);


                //newImagePathMaker(secondImageUri);
            }
            if (requestCode == THIRD_IMAGE_REQUEST) {
                //if we are here we are hearing back from the image gallery
                //Address of the image on the SD card
                thirdImageUri = data.getData();
                //declare a stream to read the image from the SD card
                InputStream thirdInputStream;
                //we are getting an inputstream based on the Uri of the image
                try {
                    thirdInputStream = getContentResolver().openInputStream(thirdImageUri);
                    //get a bitmap from th stream
                    Bitmap thirdimage = BitmapFactory.decodeStream(thirdInputStream);
                    //show tha image to the user
                    imgView3.setImageBitmap(thirdimage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //show a message to the user that image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
                //System.out.println("------"+getPath(thirdImageUri)+"------");
                //tempsceneSet.add(newImagePathMaker(thirdImageUri));
                map3.put("src", newImagePathMaker(thirdImageUri));
                map3.put("answer", "3");
                System.out.println("---Third----" + map3);


                //newImagePathMaker(thirdImageUri);
            }
        }
    }

    //to get exact URI of the image chosen
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Function which checks whether read/write permissioin is granted or not
  /*  public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                out.println("Permission is granted");
                //return true;
            } else {

                out.println("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                //return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            out.println("Permission is granted");
            //return true;
        }
    }*/

    public String newImagePathMaker(Uri imageUri) {
        String destfirstImagePath;
        destfirstImagePath = destinationFolder;
        String imgPath = getPath(imageUri);
        String newImgPath = "";
        imgPath = new StringBuilder(imgPath).reverse().toString();
        int length = imgPath.length();

        for (int i = 0; i < length; i++) {
            char c = imgPath.charAt(i);
            if (c == '/') {
                newImgPath = new StringBuilder(newImgPath).reverse().toString();

                break;
            } else {
                newImgPath += c;
            }
        }
        destfirstImagePath += newImgPath;
        System.out.println(destfirstImagePath);
        return destfirstImagePath;
    }

    public void writeToJson() throws IOException {
        data.add(0, map1);
        data.add(0, map2);
        data.add(0, map3);
        data.add(0, map1);
        data.add(0, map2);
        data.add(0, map3);


        sceneData.add(data);
        File json = new File(Environment.getExternalStorageDirectory() + "/shadow/shadow_matching_scene_data.json");
        if (json.exists())
            json.delete();
        FileOutputStream out = new FileOutputStream(json);
        JsonWriter jsonWriter = new JsonWriter((new OutputStreamWriter(out, "UTF-8")));
        jsonWriter.beginObject();
        jsonWriter.name("scenes").beginArray();


        for (ArrayList<Map<String, String>> scene : sceneData) {
            jsonWriter.beginArray();
            for (Map<String, String> entry : scene) {
                jsonWriter.beginObject();
                for (String key : entry.keySet()) {
                    jsonWriter.name(key).value(entry.get(key));
                }
                jsonWriter.endObject();
            }

            jsonWriter.endArray();

        }

        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.close();
        out.close();


    }

    public String loadJson() {
        String json = null;
        try {
            FileInputStream fis = new FileInputStream(new File(pathname));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;


    }

    public void readFromJson() throws JSONException {

        sceneData.clear();

        JSONObject obj = new JSONObject(loadJson());
        JSONArray m_jArry = obj.getJSONArray("scenes");
        HashMap<String, String> m_li;
        String src;
        String answer;

        for (int i = 0; i < m_jArry.length(); i++) {
            ArrayList formList = new ArrayList<HashMap<String, String>>();
            JSONArray jo_very_inside = m_jArry.getJSONArray(i);
            for (int j = 0; j < jo_very_inside.length(); j++) {
                JSONObject jo_inside = jo_very_inside.getJSONObject(j);
                src = jo_inside.getString("src");
                answer = jo_inside.getString("answer");
                m_li = new HashMap<String, String>();
                m_li.put("src", src);
                m_li.put("answer", answer);
                formList.add(m_li);
            }
            sceneData.add(formList);

        }
    }

}
