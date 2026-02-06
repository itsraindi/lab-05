package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private CityArrayAdapter cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    private String selectedCityId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Listen failed", error);
                return;
            }

            cityArrayList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    String name = doc.getString("name");
                    String province = doc.getString("province");
                    if (name != null && province != null) {
                        cityArrayList.add(new City(name, province));
                    }
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        addCityButton.setOnClickListener(view -> {
            selectedCityId = null;
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (city == null) return;

            selectedCityId = city.getName();

            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (city == null) return true;

            new AlertDialog.Builder(this)
                    .setTitle("Delete city")
                    .setMessage("Delete " + city.getName() + "?")
                    .setPositiveButton("Delete", (d, which) -> {
                        citiesRef.document(city.getName()).delete();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        String newName = title;
        String newProvince = year;

        Map<String, Object> data = new HashMap<>();
        data.put("name", newName);
        data.put("province", newProvince);

        String oldId = selectedCityId;
        if (oldId == null) {

            oldId = city.getName();
        }

        if (oldId.equals(newName)) {
            citiesRef.document(oldId).set(data);
        } else {
            citiesRef.document(oldId).delete();
            citiesRef.document(newName).set(data);
        }

        selectedCityId = null;
    }

    @Override
    public void addCity(City city) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", city.getName());
        data.put("province", city.getProvince());

        citiesRef.document(city.getName()).set(data);
    }

    public void addDummyData() {
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}
