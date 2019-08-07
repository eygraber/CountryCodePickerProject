package com.hbb20;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.FastScroller;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by hbb20 on 11/1/16.
 */
class CountryCodeDialog {
    private static final Field
            sEditorField,
            sCursorDrawableField,
            sCursorDrawableResourceField;
    static Dialog dialog;
    static Context context;

    static {
        Field editorField = null;
        Field cursorDrawableField = null;
        Field cursorDrawableResourceField = null;
        boolean exceptionThrown = false;
        try {
            cursorDrawableResourceField = TextView.class.getDeclaredField("mCursorDrawableRes");
            cursorDrawableResourceField.setAccessible(true);
            final Class<?> drawableFieldClass;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                drawableFieldClass = TextView.class;
            } else {
                editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                drawableFieldClass = editorField.getType();
            }
            cursorDrawableField = drawableFieldClass.getDeclaredField("mCursorDrawable");
            cursorDrawableField.setAccessible(true);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        if (exceptionThrown) {
            sEditorField = null;
            sCursorDrawableField = null;
            sCursorDrawableResourceField = null;
        } else {
            sEditorField = editorField;
            sCursorDrawableField = cursorDrawableField;
            sCursorDrawableResourceField = cursorDrawableResourceField;
        }
    }

    public static void openCountryCodeDialog(final CountryCodePicker codePicker) {
        openCountryCodeDialog(codePicker, null);
    }

    public static void
    openCountryCodeDialog(final CountryCodePicker codePicker, final String countryNameCode) {
        context = codePicker.getContext();
        dialog = new Dialog(context);
        codePicker.refreshCustomMasterList();
        codePicker.refreshPreferredCountries();
        List<CCPCountry> masterCountries = CCPCountry.getCustomMasterCountryList(context, codePicker);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_picker_dialog);

        //keyboard
        if (codePicker.isSearchAllowed() && codePicker.isDialogKeyboardAutoPopup()) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }


        //dialog views
        RecyclerView countryList = dialog.findViewById(R.id.countryList);
        final TextView title = dialog.findViewById(R.id.title);
        ImageView clearSearch = dialog.findViewById(R.id.clearSearch);
        final EditText search = dialog.findViewById(R.id.search);
        TextView emptyResults = dialog.findViewById(R.id.emptyResults);

        // type faces
        //set type faces
        try {
            if (codePicker.getDialogTypeFace() != null) {
                if (codePicker.getDialogTypeFaceStyle() != CountryCodePicker.DEFAULT_UNSET) {
                    emptyResults.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                    search.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                    title.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                } else {
                    emptyResults.setTypeface(codePicker.getDialogTypeFace());
                    search.setTypeface(codePicker.getDialogTypeFace());
                    title.setTypeface(codePicker.getDialogTypeFace());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //dialog background color
        if (codePicker.getDialogBackgroundColor() != 0) {
            // TODO
//            rlHolder.setBackgroundColor(codePicker.getDialogBackgroundColor());
        }

        //title
        if (!codePicker.getCcpDialogShowTitle()) {
            title.setVisibility(View.GONE);
        }

        //clear button color and title color
        if (codePicker.getDialogTextColor() != 0) {
            int textColor = codePicker.getDialogTextColor();
            clearSearch.setColorFilter(textColor);
            title.setTextColor(textColor);
            emptyResults.setTextColor(textColor);
            search.setTextColor(textColor);
            search.setHintTextColor(Color.argb(100, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
        }


        //editText tint
        if (codePicker.getDialogSearchEditTextTintColor() != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                search.setBackgroundTintList(ColorStateList.valueOf(codePicker.getDialogSearchEditTextTintColor()));
                setCursorColor(search, codePicker.getDialogSearchEditTextTintColor());
            }
        }


        //add messages to views
        title.setText(codePicker.getDialogTitle());
        search.setHint(codePicker.getSearchHintText());
        emptyResults.setText(codePicker.getNoResultACK());

        if(!codePicker.isSearchAllowed()) {
            search.setVisibility(View.GONE);
            clearSearch.setVisibility(View.GONE);
        }

        final CountryCodeAdapter cca = new CountryCodeAdapter(context, masterCountries, codePicker, search, emptyResults, dialog, clearSearch);
        countryList.setLayoutManager(new LinearLayoutManager(context));
        countryList.setAdapter(cca);

        //fast scroller
        FastScroller fastScroller = dialog.findViewById(R.id.fastscroll);
        fastScroller.setRecyclerView(countryList);
        if (codePicker.isShowFastScroller()) {
            if (codePicker.getFastScrollerBubbleColor() != 0) {
                fastScroller.setBubbleColor(codePicker.getFastScrollerBubbleColor());
            }

            if (codePicker.getFastScrollerHandleColor() != 0) {
                fastScroller.setHandleColor(codePicker.getFastScrollerHandleColor());
            }

            if (codePicker.getFastScrollerBubbleTextAppearance() != 0) {
                try {
                    fastScroller.setBubbleTextAppearance(codePicker.getFastScrollerBubbleTextAppearance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            fastScroller.setVisibility(View.GONE);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                hideKeyboard(context);
                if (codePicker.getDialogEventsListener() != null) {
                    codePicker.getDialogEventsListener().onCcpDialogDismiss(dialogInterface);
                }
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                hideKeyboard(context);
                if (codePicker.getDialogEventsListener() != null) {
                    codePicker.getDialogEventsListener().onCcpDialogCancel(dialogInterface);
                }
            }
        });

        //auto scroll to mentioned countryNameCode
        if (countryNameCode != null) {
            boolean isPreferredCountry = false;
            if (codePicker.preferredCountries != null) {
                for (CCPCountry preferredCountry : codePicker.preferredCountries) {
                    if (preferredCountry.nameCode.equalsIgnoreCase(countryNameCode)) {
                        isPreferredCountry = true;
                        break;
                    }
                }
            }

            //if selection is from preferred countries then it should show all (or maximum) preferred countries.
            // don't scroll if it was one of those preferred countries
            if (!isPreferredCountry) {
                int preferredCountriesOffset = 0;
                if (codePicker.preferredCountries != null && codePicker.preferredCountries.size() > 0) {
                    preferredCountriesOffset = codePicker.preferredCountries.size() + 1; //+1 is for divider
                }
                for (int i = 0; i < masterCountries.size(); i++) {
                    if (masterCountries.get(i).nameCode.equalsIgnoreCase(countryNameCode)) {
                        countryList.scrollToPosition(i + preferredCountriesOffset);
                        break;
                    }
                }
            }
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.show();

        dialog.getWindow().setAttributes(lp);
        if (codePicker.getDialogEventsListener() != null) {
            codePicker.getDialogEventsListener().onCcpDialogOpen(dialog);
        }
    }

    private static void hideKeyboard(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    static void setCursorColor(EditText editText, int color) {
        if (sCursorDrawableField == null) {
            return;
        }
        try {
            final Drawable drawable = getDrawable(editText.getContext(),
                    sCursorDrawableResourceField.getInt(editText));
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            sCursorDrawableField.set(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN
                    ? editText : sEditorField.get(editText), new Drawable[]{drawable, drawable});
        } catch (Exception ignored) {
        }
    }

    static void clear() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = null;
        context = null;
    }

    private static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id);
        } else {
            return context.getDrawable(id);
        }
    }
}
