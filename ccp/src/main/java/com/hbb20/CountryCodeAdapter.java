package com.hbb20;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbb20 on 11/1/16.
 */
class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> implements SectionTitleProvider {
    private List<CCPCountry> filteredCountries, masterCountries;
    private TextView emptyResults;
    private CountryCodePicker codePicker;
    private LayoutInflater inflater;
    private EditText search;
    private Dialog dialog;
    private Context context;
    private ImageView clearSearch;
    private int preferredCountriesCount = 0;

    CountryCodeAdapter(Context context, List<CCPCountry> countries, CountryCodePicker codePicker, final EditText search, TextView emptyResults, Dialog dialog, ImageView clearSearch) {
        this.context = context;
        this.masterCountries = countries;
        this.codePicker = codePicker;
        this.dialog = dialog;
        this.emptyResults = emptyResults;
        this.search = search;
        this.clearSearch = clearSearch;
        this.inflater = LayoutInflater.from(context);
        this.filteredCountries = getFilteredCountries("");

        if(codePicker.isSearchAllowed()) {
            setTextWatcher();
            setQueryClearListener();
        }
    }

    private void setQueryClearListener() {
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setText("");
            }
        });
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private void setTextWatcher() {
        if (this.search != null) {
            this.search.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyQuery(s.toString());
                    if (s.toString().trim().equals("")) {
                        TransitionManager.beginDelayedTransition((ViewGroup) clearSearch.getParent());
                        clearSearch.setVisibility(View.GONE);
                    } else {
                        TransitionManager.beginDelayedTransition((ViewGroup) clearSearch.getParent());
                        clearSearch.setVisibility(View.VISIBLE);
                    }
                }
            });

            this.search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(search.getWindowToken(), 0);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private void applyQuery(String query) {


        emptyResults.setVisibility(View.GONE);
        query = query.toLowerCase();

        //if query started from "+" ignore it
        if (query.length() > 0 && query.charAt(0) == '+') {
            query = query.substring(1);
        }

        filteredCountries = getFilteredCountries(query);

        if (filteredCountries.size() == 0) {
            emptyResults.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    private List<CCPCountry> getFilteredCountries(String query) {
        List<CCPCountry> tempCCPCountryList = new ArrayList<CCPCountry>();
        preferredCountriesCount = 0;
        if (codePicker.preferredCountries != null && codePicker.preferredCountries.size() > 0) {
            for (CCPCountry CCPCountry : codePicker.preferredCountries) {
                if (CCPCountry.isEligibleForQuery(query)) {
                    tempCCPCountryList.add(CCPCountry);
                    preferredCountriesCount++;
                }
            }

            if (tempCCPCountryList.size() > 0) { //means at least one preferred country is added.
                CCPCountry divider = null;
                tempCCPCountryList.add(divider);
                preferredCountriesCount++;
            }
        }

        for (CCPCountry CCPCountry : masterCountries) {
            if (CCPCountry.isEligibleForQuery(query)) {
                tempCCPCountryList.add(CCPCountry);
            }
        }
        return tempCCPCountryList;
    }

    @Override
    public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View rootView = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false);
        CountryCodeViewHolder viewHolder = new CountryCodeViewHolder(rootView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CountryCodeViewHolder countryCodeViewHolder, final int i) {
        countryCodeViewHolder.setCountry(filteredCountries.get(i));
        if (filteredCountries.size() > i && filteredCountries.get(i) != null) {
            countryCodeViewHolder.getMainView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (filteredCountries != null && filteredCountries.size() > i) {
                        codePicker.onUserTappedCountry(filteredCountries.get(i));
                    }
                    if (view != null && filteredCountries != null && filteredCountries.size() > i && filteredCountries.get(i) != null) {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                }
            });
        } else {
            countryCodeViewHolder.getMainView().setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return filteredCountries.size();
    }

    @Override
    public String getSectionTitle(int position) {
        CCPCountry ccpCountry = filteredCountries.get(position);
        if (preferredCountriesCount > position) {
            return "★";
        } else if (ccpCountry != null) {
            return ccpCountry.getName().substring(0, 1);
        } else {
            return "☺"; //this should never be the case
        }
    }

    class CountryCodeViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout root;
        TextView name, code;
        View divider;

        public CountryCodeViewHolder(View itemView) {
            super(itemView);
            root = (ConstraintLayout) itemView;
            name = root.findViewById(R.id.countryName);
            code = root.findViewById(R.id.code);
            divider = root.findViewById(R.id.preferenceDivider);

            if (codePicker.getDialogTextColor() != 0) {
                name.setTextColor(codePicker.getDialogTextColor());
                code.setTextColor(codePicker.getDialogTextColor());
                divider.setBackgroundColor(codePicker.getDialogTextColor());
            }

            try {
                if (codePicker.getDialogTypeFace() != null) {
                    if (codePicker.getDialogTypeFaceStyle() != CountryCodePicker.DEFAULT_UNSET) {
                        code.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                        name.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                    } else {
                        code.setTypeface(codePicker.getDialogTypeFace());
                        name.setTypeface(codePicker.getDialogTypeFace());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setCountry(CCPCountry ccpCountry) {
            if (ccpCountry != null) {
                divider.setVisibility(View.GONE);
                name.setVisibility(View.VISIBLE);
                code.setVisibility(View.VISIBLE);
                if (codePicker.isCcpDialogShowPhoneCode()) {
                    code.setVisibility(View.VISIBLE);
                } else {
                    code.setVisibility(View.GONE);
                }

                String countryName = "";

                if (codePicker.getCcpDialogShowFlag()) {
                    //extra space is just for alignment purpose
                    countryName += CCPCountry.getFlagEmoji(ccpCountry) + "   ";
                }

                countryName += ccpCountry.getName();

                if (codePicker.getCcpDialogShowNameCode()) {
                    countryName += " (" + ccpCountry.getNameCode().toUpperCase() + ")";
                }

                name.setText(countryName);
                code.setText("+" + ccpCountry.getPhoneCode());
            } else {
                divider.setVisibility(View.VISIBLE);
                name.setVisibility(View.GONE);
                code.setVisibility(View.GONE);
            }
        }

        public ConstraintLayout getMainView() {
            return root;
        }
    }
}

