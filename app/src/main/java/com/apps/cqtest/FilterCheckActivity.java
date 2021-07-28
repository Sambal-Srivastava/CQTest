package com.apps.cqtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class FilterCheckActivity extends AppCompatActivity {

    private Button btnFinish;
    private ImageButton btnText, btnShape, btnUndo, btnRedo;
    private PhotoEditorView mPhotoEditorView;
    private PhotoEditor mPhotoEditor;
    private ShapeBuilder mShapeBuilder;
    private EditText etLabel;
    private Bitmap saveNewBitmap;
    private LinearLayout llText;
    private ImageView ivDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_check);
        setDataToViews();
        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
        //================check intent========================
        Intent intent = getIntent();
        if (intent.hasExtra("image")) {
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            mPhotoEditorView.getSource().setImageBitmap(bmp);
        }
        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llText.setVisibility(View.VISIBLE);
                etLabel.requestFocus();
                etLabel.setText(null);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etLabel, InputMethodManager.SHOW_IMPLICIT);

            }
        });
        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etLabel.getText().toString())) {
                    mPhotoEditor.addText(etLabel.getText().toString(), getResources().getColor(R.color.white));
                    llText.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etLabel.getWindowToken(), 0);
                } else {
                    CommonMethods.displayMessage(FilterCheckActivity.this, getResources().getString(R.string.empty_text_field));
                }
            }
        });

        btnShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSquare();
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoEditor.undo();
            }
        });
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoEditor.redo();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//======================show progress dialog=====================
                CommonMethods.showProgressDialog(FilterCheckActivity.this, "Saving Changes");
                mPhotoEditor.saveAsBitmap(new OnSaveBitmap() {
                    @Override
                    public void onBitmapReady(Bitmap saveBitmap) {


                        //======================

                        try {
                            //Write file
                            String filename = "bitmap.png";
                            FileOutputStream stream = FilterCheckActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);
                            saveBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);

                            //Cleanup
                            stream.close();
                            saveBitmap.recycle();

                            //Pop intent
                            Intent in1 = new Intent();
                            in1.putExtra("editedImageResult", filename);
                            setResult(Activity.RESULT_OK, in1);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(FilterCheckActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(FilterCheckActivity.this, "Changes not saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setDataToViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView);
        etLabel = findViewById(R.id.etLabel);
        btnText = findViewById(R.id.btnText);
        btnShape = findViewById(R.id.btnShape);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnFinish = findViewById(R.id.btnFinish);
        llText = findViewById(R.id.llText);
        ivDone = findViewById(R.id.ivDone);
    }

    private void createSquare() {
        mPhotoEditor.setBrushDrawingMode(true);
        mShapeBuilder = new ShapeBuilder()
                .withShapeOpacity(100)
                .withShapeType(ShapeType.RECTANGLE)
                .withShapeColor(getResources().getColor(R.color.white))
                .withShapeSize(20);
        mPhotoEditor.setShape(mShapeBuilder);

    }
}
