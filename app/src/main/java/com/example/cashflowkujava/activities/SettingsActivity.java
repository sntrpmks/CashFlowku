package com.example.cashflowkujava.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashflowkujava.R;
import com.example.cashflowkujava.adapters.ProductAdapter;
import com.example.cashflowkujava.database.DatabaseHelper;
import com.example.cashflowkujava.models.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final int REQUEST_CODE_CAMERA = 1001;
    private static final int REQUEST_CODE_GALLERY = 1002;

    private SwitchCompat switchTheme;
    private EditText etSearch;
    private RecyclerView rvProducts;
    private TextView tvEmptyState;

    private DatabaseHelper dbHelper;
    private List<Product> productsList;
    private ProductAdapter adapter;

    private String filterQuery = "";
    private final String[] categories = {"Makanan", "Minuman", "Laundry", "Pakaian", "Jasa", "Lain-lain"};

    private SharedPreferences themePrefs;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    // Dialog state variables
    private String currentDialogPhotoPath = "";
    private Button btnDialogAddPhotoReference = null;
    private File cameraTempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        themePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Bind Views
        switchTheme = findViewById(R.id.switch_dark_mode);
        etSearch = findViewById(R.id.et_search_products);
        rvProducts = findViewById(R.id.rv_products_settings);
        tvEmptyState = findViewById(R.id.tv_empty_products);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        findViewById(R.id.btn_back_settings).setOnClickListener(v -> finish());

        // Theme setup
        boolean isDarkMode = themePrefs.getBoolean(KEY_DARK_MODE, false);
        switchTheme.setChecked(isDarkMode);
        
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themePrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            dbHelper.saveSetting(KEY_DARK_MODE, String.valueOf(isChecked));
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuery = s.toString();
                loadProductsData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Product trigger
        findViewById(R.id.fab_add_product).setOnClickListener(v -> showAddEditProductDialog(null));

        loadProductsData();
    }

    private void loadProductsData() {
        List<Product> all = dbHelper.getAllProducts();
        productsList = new ArrayList<>();
        
        for (Product p : all) {
            if (filterQuery.isEmpty() || p.getName().toLowerCase().contains(filterQuery.toLowerCase())) {
                productsList.add(p);
            }
        }

        if (productsList.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter = new ProductAdapter(productsList, this);
            rvProducts.setAdapter(adapter);
        }
    }

    private void showAddEditProductDialog(final Product productToEdit) {
        boolean isEdit = (productToEdit != null);

        // Set state values
        currentDialogPhotoPath = isEdit && productToEdit.getImagePath() != null ? productToEdit.getImagePath() : "";

        // Custom Layout creation
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        int pad = (int) (10 * getResources().getDisplayMetrics().density);

        // Product Name Input
        TextView tvLabelName = new TextView(this);
        tvLabelName.setText("Nama Produk:");
        tvLabelName.setPadding(0, 16, 0, 8);
        tvLabelName.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelName);

        EditText etName = new EditText(this);
        etName.setHint("Masukkan nama produk...");
        etName.setBackgroundResource(R.drawable.bg_input);
        etName.setPadding(pad, pad, pad, pad);
        etName.setTextColor(getResources().getColor(R.color.textPrimary));
        etName.setHintTextColor(getResources().getColor(R.color.textMuted));
        etName.setTextSize(14);
        if (isEdit) etName.setText(productToEdit.getName());
        layout.addView(etName);

        // Category Spinner
        TextView tvLabelCat = new TextView(this);
        tvLabelCat.setText("Kategori:");
        tvLabelCat.setPadding(0, 16, 0, 8);
        tvLabelCat.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelCat);

        Spinner spCat = new Spinner(this);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCat.setAdapter(catAdapter);
        spCat.setBackgroundResource(R.drawable.bg_spinner);
        spCat.setPadding(pad, pad, pad + (int)(24 * getResources().getDisplayMetrics().density), pad);
        if (isEdit) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(productToEdit.getCategory())) {
                    spCat.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spCat);

        // Price Input (Harga Jual)
        TextView tvLabelPrice = new TextView(this);
        tvLabelPrice.setText("Harga Jual (Rp):");
        tvLabelPrice.setPadding(0, 16, 0, 8);
        tvLabelPrice.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelPrice);

        EditText etPrice = new EditText(this);
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setHint("0");
        etPrice.setBackgroundResource(R.drawable.bg_input);
        etPrice.setPadding(pad, pad, pad, pad);
        etPrice.setTextColor(getResources().getColor(R.color.textPrimary));
        etPrice.setHintTextColor(getResources().getColor(R.color.textMuted));
        etPrice.setTextSize(14);
        if (isEdit) etPrice.setText(String.format(java.util.Locale.US, "%.0f", productToEdit.getPrice()));
        layout.addView(etPrice);

        // Capital/Modal Input (Harga Beli / Modal) — Opsional
        TextView tvLabelModal = new TextView(this);
        tvLabelModal.setText("Harga Modal / Modal Awal (Rp):");
        tvLabelModal.setPadding(0, 16, 0, 8);
        tvLabelModal.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelModal);

        EditText etModal = new EditText(this);
        etModal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etModal.setHint("0");
        etModal.setBackgroundResource(R.drawable.bg_input);
        etModal.setPadding(pad, pad, pad, pad);
        etModal.setTextColor(getResources().getColor(R.color.textPrimary));
        etModal.setHintTextColor(getResources().getColor(R.color.textMuted));
        etModal.setTextSize(14);
        if (isEdit) etModal.setText(String.format(java.util.Locale.US, "%.0f", productToEdit.getModal()));
        layout.addView(etModal);

        // Stock Input
        TextView tvLabelStock = new TextView(this);
        tvLabelStock.setText("Jumlah Stok:");
        tvLabelStock.setPadding(0, 16, 0, 8);
        tvLabelStock.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelStock);

        EditText etStock = new EditText(this);
        etStock.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etStock.setHint("0");
        etStock.setBackgroundResource(R.drawable.bg_input);
        etStock.setPadding(pad, pad, pad, pad);
        etStock.setTextColor(getResources().getColor(R.color.textPrimary));
        etStock.setHintTextColor(getResources().getColor(R.color.textMuted));
        etStock.setTextSize(14);
        if (isEdit) etStock.setText(String.valueOf(productToEdit.getStock()));
        layout.addView(etStock);

        // Photo Upload Button
        TextView tvLabelPhoto = new TextView(this);
        tvLabelPhoto.setText("Foto Produk:");
        tvLabelPhoto.setPadding(0, 16, 0, 8);
        tvLabelPhoto.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabelPhoto);

        btnDialogAddPhotoReference = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnDialogAddPhotoReference.setBackgroundResource(R.drawable.bg_input);
        btnDialogAddPhotoReference.setPadding(pad, pad, pad, pad);
        btnDialogAddPhotoReference.setTextColor(getResources().getColor(R.color.textPrimary));
        if (!currentDialogPhotoPath.isEmpty()) {
            btnDialogAddPhotoReference.setText("✓ Foto Terpilih (Ketuk untuk ubah)");
        } else {
            btnDialogAddPhotoReference.setText("📸 Unggah Foto Produk");
        }
        layout.addView(btnDialogAddPhotoReference);
        btnDialogAddPhotoReference.setOnClickListener(v -> showImagePickerDialog());

        // Wrap in ScrollView
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.addView(layout);

        // Build dialog with builder.setPositiveButton (ensures buttons always visible)
        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Detail Produk" : "Tambah Produk Baru")
                .setView(scrollView)
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    String name = etName.getText().toString().trim();
                    String cat = spCat.getSelectedItem().toString();
                    String priceStr = etPrice.getText().toString().trim();
                    String modalStr = etModal.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();

                    // Validasi: nama, harga, modal, stok wajib diisi
                    if (name.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, "Nama produk tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (priceStr.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, "Harga jual tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (stockStr.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, "Jumlah stok tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double price = Double.parseDouble(priceStr);
                    double modal = modalStr.isEmpty() ? 0.0 : Double.parseDouble(modalStr);
                    int stock = Integer.parseInt(stockStr);

                    if (isEdit) {
                        productToEdit.setName(name);
                        productToEdit.setCategory(cat);
                        productToEdit.setPrice(price);
                        productToEdit.setModal(modal);
                        productToEdit.setStock(stock);
                        productToEdit.setImagePath(currentDialogPhotoPath);

                        int rows = dbHelper.updateProduct(productToEdit);
                        if (rows > 0) {
                            Toast.makeText(SettingsActivity.this, "Produk berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                            loadProductsData();
                        }
                    } else {
                        Product newProduct = new Product(name, price, modal, stock, cat, currentDialogPhotoPath);
                        long id = dbHelper.insertProduct(newProduct);
                        if (id != -1) {
                            Toast.makeText(SettingsActivity.this, "Produk baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                            loadProductsData();
                        }
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void showImagePickerDialog() {
        String[] options = {"Ambil Foto (Kamera)", "Pilih dari Galeri"};
        new AlertDialog.Builder(this)
                .setTitle("Unggah Foto Produk")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                })
                .show();
    }

    private void launchCamera() {
        try {
            cameraTempFile = new File(getCacheDir(), "temp_camera_photo.jpg");
            if (cameraTempFile.exists()) {
                cameraTempFile.delete();
            }
            cameraTempFile.createNewFile();

            Uri photoUri = FileProvider.getUriForFile(this, 
                    "com.example.cashflowkujava.fileprovider", cameraTempFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat file sementara untuk kamera", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        File internalFile = new File(getFilesDir(), "product_img_" + System.currentTimeMillis() + ".jpg");

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (cameraTempFile != null && cameraTempFile.exists()) {
                if (copyFile(cameraTempFile, internalFile)) {
                    currentDialogPhotoPath = internalFile.getAbsolutePath();
                    if (btnDialogAddPhotoReference != null) {
                        btnDialogAddPhotoReference.setText("✓ Foto Terpilih (Kamera)");
                    }
                    Toast.makeText(this, "Foto kamera berhasil dilampirkan!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_GALLERY && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            if (copyStreamToFile(selectedUri, internalFile)) {
                currentDialogPhotoPath = internalFile.getAbsolutePath();
                if (btnDialogAddPhotoReference != null) {
                    btnDialogAddPhotoReference.setText("✓ Foto Terpilih (Galeri)");
                }
                Toast.makeText(this, "Foto galeri berhasil dilampirkan!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean copyStreamToFile(Uri srcUri, File destFile) {
        try (InputStream is = getContentResolver().openInputStream(srcUri);
             FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyalin gambar galeri", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean copyFile(File src, File dst) {
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dst)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyalin gambar kamera", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onProductClick(Product product) {
        showAddEditProductDialog(product);
    }

    @Override
    public void onProductLongClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Apakah Anda yakin ingin menghapus produk " + product.getName() + " dari daftar inventory?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int rows = dbHelper.deleteProduct(product.getId());
                    if (rows > 0) {
                        Toast.makeText(SettingsActivity.this, "Produk berhasil dihapus.", Toast.LENGTH_SHORT).show();
                        loadProductsData();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onProductEdit(Product product) {
        showAddEditProductDialog(product);
    }

    @Override
    public void onProductRestock(Product product) {
        showRestockDialog(product);
    }

    private void showRestockDialog(final Product product) {
        int pad = (int) (10 * getResources().getDisplayMetrics().density);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        // Current stock info
        TextView tvCurrentStock = new TextView(this);
        tvCurrentStock.setText("Stok saat ini: " + product.getStock() + " unit");
        tvCurrentStock.setPadding(0, 8, 0, 8);
        tvCurrentStock.setTextColor(getResources().getColor(R.color.textSecondary));
        tvCurrentStock.setTextSize(14);
        layout.addView(tvCurrentStock);

        // Label
        TextView tvLabel = new TextView(this);
        tvLabel.setText("Jumlah stok yang ditambahkan:");
        tvLabel.setPadding(0, 16, 0, 8);
        tvLabel.setTextColor(getResources().getColor(R.color.textSecondary));
        layout.addView(tvLabel);

        EditText etAddQty = new EditText(this);
        etAddQty.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etAddQty.setHint("Contoh: 50");
        etAddQty.setBackgroundResource(R.drawable.bg_input);
        etAddQty.setPadding(pad, pad, pad, pad);
        etAddQty.setTextColor(getResources().getColor(R.color.textPrimary));
        etAddQty.setHintTextColor(getResources().getColor(R.color.textMuted));
        etAddQty.setTextSize(14);
        layout.addView(etAddQty);

        // New stock preview
        TextView tvNewStock = new TextView(this);
        tvNewStock.setText("Stok baru: " + product.getStock());
        tvNewStock.setPadding(0, 16, 0, 8);
        tvNewStock.setTextSize(16);
        tvNewStock.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNewStock.setTextColor(getResources().getColor(R.color.colorIncome));
        layout.addView(tvNewStock);

        etAddQty.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int add = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                    tvNewStock.setText("Stok baru: " + (product.getStock() + add));
                } catch (NumberFormatException e) {
                    tvNewStock.setText("Stok baru: " + product.getStock());
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Tambah Stok: " + product.getName())
                .setView(layout)
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    String qtyStr = etAddQty.getText().toString().trim();
                    if (qtyStr.isEmpty()) {
                        Toast.makeText(this, "Masukkan jumlah stok yang ditambahkan!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int addQty = Integer.parseInt(qtyStr);
                    if (addQty <= 0) {
                        Toast.makeText(this, "Jumlah harus lebih dari 0!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    product.setStock(product.getStock() + addQty);
                    int rows = dbHelper.updateProduct(product);
                    if (rows > 0) {
                        Toast.makeText(this, "Stok berhasil ditambah " + addQty + " unit!", Toast.LENGTH_SHORT).show();
                        loadProductsData();
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }
}
