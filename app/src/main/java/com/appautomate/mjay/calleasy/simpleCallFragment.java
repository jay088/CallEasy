package com.appautomate.mjay.calleasy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class simpleCallFragment extends Fragment {

    //declare layout component references
    Button callButton;
    Button savePrefButton;
    AutoCompleteTextView autCompTxtVwAccessNum;
    EditText edtTxtPrefix;
    AutoCompleteTextView autCompTxtVwPhoneNum;
    CheckBox checkBoxCountryCode;
    CheckBox checkBoxPin;

    //miscellaneous
    SimpleAdapter autoCompleteAdapter;
    ArrayList<Map<String, String>> contactList = new ArrayList<Map<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View simpleCallFragment = inflater.inflate(R.layout.fragment_simple_call,container,false);

        //intitialize references to layout components
        autCompTxtVwAccessNum = (AutoCompleteTextView) simpleCallFragment.findViewById(R.id.access_number);
        autCompTxtVwPhoneNum = (AutoCompleteTextView) simpleCallFragment.findViewById(R.id.target_number);
        edtTxtPrefix = (EditText) simpleCallFragment.findViewById(R.id.prefix_number);
        callButton = (Button) simpleCallFragment.findViewById(R.id.call_button);
        savePrefButton = (Button) simpleCallFragment.findViewById(R.id.save_button);
        checkBoxCountryCode = (CheckBox) simpleCallFragment.findViewById(R.id.countryCheckBox);
        checkBoxPin = (CheckBox) simpleCallFragment.findViewById(R.id.pinCheckBox);


        SharedPreferences userPreferences = this.getActivity().getSharedPreferences("CALLEASY_USR_PREFS",0);

        //set saved Access number & Prefix from previously saved user preferences (if they exist).

        String isCountryCodePrefChecked = userPreferences.getString("CALLEASY_COUNTRY_CODE_PREF","").toString();
        if(isCountryCodePrefChecked.equals("true")){
            checkBoxCountryCode.setChecked(true);
        }
        else{
            checkBoxCountryCode.setChecked(false);
        }

        String isPinPrefChecked = userPreferences.getString("CALLEASY_PIN_PREF","").toString();
        if(isPinPrefChecked.equals("true")){
            checkBoxPin.setChecked(true);
        }
        else{
            checkBoxPin.setChecked(false);
        }

        String accessNumTemp = userPreferences.getString("CALLEASY_ACCESS_NUM", "");
        //TODO replace with function
        accessNumTemp = accessNumTemp.trim();
        accessNumTemp = accessNumTemp.replace(" ", "");
        accessNumTemp = accessNumTemp.replaceAll("[^0-9]","");
        autCompTxtVwAccessNum.setText(accessNumTemp);

        String prefixTemp = userPreferences.getString("CALLEASY_USR_PREFIX","");
        prefixTemp = prefixTemp.trim();
        prefixTemp = prefixTemp.replace(" ", "");
        prefixTemp = prefixTemp.replaceAll("[^0-9]","");
        edtTxtPrefix.setText(prefixTemp);


        //fetch the Phone's contact list (native contacts) into a common ArrayList "contactList" for both input boxes to use
        populateContactList();
        Log.v("VERBOSE_POPULATE_PHONELIST","3. Lists populated");
        autoCompleteAdapter = new SimpleAdapter(this.getActivity(),contactList, R.layout.customcontactview, new String[] { "Name", "Phone" }, new int[] { R.id.contName, R.id.contNum });

        Log.v("VERBOSE_POPULATE_PHONELIST","4. Lists populated");

        //populate the contact List into the View component for Access Num
        autCompTxtVwAccessNum.setThreshold(1);
        Log.v("VERBOSE_POPULATE_PHONELIST","5. Lists populated");
        autCompTxtVwAccessNum.setAdapter(autoCompleteAdapter);
        Log.v("VERBOSE_POPULATE_PHONELIST","6. Lists populated");

        //populate the contact List into the View component for Target Num; reuse previous adapter
        autCompTxtVwPhoneNum.setThreshold(1);
        Log.v("VERBOSE_POPULATE_PHONELIST","7. Lists populated");
        autCompTxtVwPhoneNum.setAdapter(autoCompleteAdapter);
        Log.v("VERBOSE_POPULATE_PHONELIST","8. Lists populated");


        Log.v("VERBOSE_POPULATE_PHONELIST","2. Lists populated");

        //******************************************************************LISTENERS******************************************************************

        //[Enter Pin Checkbox listener] Handles the event of showing the dialog when some clicks to enter a PIN.
        checkBoxPin.setOnClickListener(new View.OnClickListener(){

            Activity activity = getActivity();

            @Override
            public void onClick(View view){
                if(checkBoxPin.isChecked()){

                    final Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.pin_option_dialog);
                    dialog.setTitle("Please enter PIN");

                    //initiate references for layout components
                    TextView pinPrefTextView = (TextView) dialog.findViewById(R.id.pinTxtVw);
                    final EditText pinEditText = (EditText) dialog.findViewById(R.id.pinEditText);
                    Button pinSaveButton = (Button) dialog.findViewById(R.id.pinOptionSaveButton);
                    Button pinCancelButton = (Button) dialog.findViewById(R.id.pinOptionCancelButton);

                    //set the PIN if previously saved by user
                    SharedPreferences userPINPreferences = activity.getSharedPreferences("CALLEASY_USER_PREFS",0);
                    pinEditText.setText(userPINPreferences.getString("CALLEASY_PIN_PREF",""));

                    //[PIN Save button listener] Handles the event of validating & saving the PIN

                    pinSaveButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view){
                            String pin =  pinEditText.getText().toString();

                            try{
                                int pinNum = Integer.parseInt(pin);
                            }
                            catch(NumberFormatException e){
                                Toast.makeText(activity, "Please enter a valid PIN!",Toast.LENGTH_LONG).show();
                                return;
                            }

                            SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                            SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                            userPrefEditor.putString("CALLEASY_PIN_PREF","true");
                            userPrefEditor.putString("CALLEASY_PIN",pin);
                            userPrefEditor.commit();
                            Toast.makeText(activity, "PIN preferences saved! :)", Toast.LENGTH_LONG).show();
                            checkBoxPin.setChecked(true);
                            dialog.dismiss();

                        }
                    });

                    //[PIN Cancel Button Listener] Handles the event of cancelling the PIN dialog box and returning to previous fragment screen

                    pinCancelButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            checkBoxPin.setChecked(false);
                            Log.v("VERBOSE_PHONE_TAG","In Cancel button on Click Listener");
                            SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                            SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                            userPrefEditor.putString("CALLEASY_PIN_PREF","false");
                            userPrefEditor.commit();
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
                if(!checkBoxPin.isChecked()){
                    SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                    SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                    userPrefEditor.putString("CALLEASY_PIN_PREF","false");
                    userPrefEditor.commit();
                }
            }
        });


        //[CountryCode Checkbox listener] Handles the event of showing the dialog when someone agrees to have country code removed.

        checkBoxCountryCode.setOnClickListener(new View.OnClickListener() {

            Activity activity = getActivity();

            @Override
            public void onClick(View view) {
                if(checkBoxCountryCode.isChecked()){

                    final Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.country_code_option_dialog);
                    dialog.setTitle("Please enter Country Code Preferences");

                    // initiate references for layout components
                    TextView checkBoxCountryCodeTextView = (TextView) dialog.findViewById(R.id.countryCodeTextView);
                    final EditText checkBoxCountryCodeEditText = (EditText) dialog.findViewById(R.id.countryCodeEditText);
                    Button checkBoxCountrySaveButton = (Button) dialog.findViewById(R.id.countryCodeSaveButton);
                    Button checkBoxCountryCancelButton = (Button) dialog.findViewById(R.id.countryCodeCancelButton);

                    //set the country code if previously saved by user
                    SharedPreferences userCountryCodePreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                    checkBoxCountryCodeEditText.setText(userCountryCodePreferences.getString("CALLEASY_COUNTRY_CODE", ""));

                    //[Save Button Listener] Handles the event of validating & saving the country code

                    checkBoxCountrySaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String countryCode = checkBoxCountryCodeEditText.getText().toString();

                            if(countryCode.length()>3){
                                Toast.makeText(activity, "Please enter a valid country code!",Toast.LENGTH_LONG).show();
                                return;
                            }

                            try{
                                int countryCodeNum = Integer.parseInt(countryCode);
                            }
                            catch(NumberFormatException e){
                                Toast.makeText(activity, "Please enter a valid country code!",Toast.LENGTH_LONG).show();
                                return;
                            }

                            SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                            SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                            userPrefEditor.putString("CALLEASY_COUNTRY_CODE_PREF","true");
                            userPrefEditor.putString("CALLEASY_COUNTRY_CODE",countryCode);
                            userPrefEditor.commit();
                            Toast.makeText(activity, "Country Code preferences Saved! :)", Toast.LENGTH_LONG).show();
                            checkBoxCountryCode.setChecked(true);
                            dialog.dismiss();
                        }
                    });

                    //[Cancel Button Listener] Handles the event of cancelling the country code dialog box and returning to previous fragment screen

                    checkBoxCountryCancelButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            checkBoxCountryCode.setChecked(false);
                            Log.v("VERBOSE_PHONE_TAG","In Cancel button on Click Listener");
                            SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                            SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                            userPrefEditor.putString("CALLEASY_COUNTRY_CODE_PREF","false");
                            userPrefEditor.commit();
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
                if(!checkBoxCountryCode.isChecked()){
                    SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                    SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                    userPrefEditor.putString("CALLEASY_COUNTRY_CODE_PREF","false");
                    userPrefEditor.commit();
                }
            }
        });

        //[AccessNum - Contacts Clicked Listener] listener for handling user's input into accessNum and displaying the final trimmed number

        autCompTxtVwAccessNum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            String accessNum = null;
            String accessNumFinal = null;

            Map<String, String> map = (Map<String, String>) adapterView.getItemAtPosition(i);

            accessNum = map.get("Phone").toString();

            Log.v("VERBOSE_PHONE_TAG","ACCESS NUM LISTENER. Access Num Original = "+accessNum);

                accessNumFinal = accessNum.trim();
            accessNumFinal = accessNumFinal.trim();
            accessNumFinal = accessNumFinal.replace(" ", "");
            accessNumFinal = accessNumFinal.replaceAll("[^0-9]","");


            Log.v("VERBOSE_PHONE_TAG","ACCESS NUM LISTENER. Access Num Final = "+accessNumFinal);

            autCompTxtVwAccessNum.setText(accessNumFinal);
            accessNumFinal = null;
            accessNum = null;
            }
        });


        //[TargetNum - Contacts Clicked Listener] listener for handling user's input into targetNum and displaying the final trimmed number

        autCompTxtVwPhoneNum.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            Activity activity = getActivity();

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                boolean isCountryCodeAvail = false;

                SharedPreferences userCountryCodePreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS",0);
                String countryCode = userCountryCodePreferences.getString("CALLEASY_COUNTRY_CODE", "");
                countryCode = countryCode.replaceAll("[^0-9]","");

                try{
                    int countryCodeNum = Integer.parseInt(countryCode);
                }
                catch(NumberFormatException e){
                    Toast.makeText(activity, "Error. Country Code not as exptected!",Toast.LENGTH_LONG).show();
                    return;
                }

                if((countryCode.length()<=3) && (countryCode.length()>=1)){
                    isCountryCodeAvail = true;
                }

                if (isCountryCodeAvail){
                    countryCode = countryCode.trim();
                    countryCode.replace(" ","");
                    countryCode = countryCode.replaceFirst("^0+(?!$)","");
                }
                else{
                    countryCode="";
                }

                String targetNum = null;
                String targetNumFinal = null;
                String tnFinal = null;

                Map<String, String> map = (Map<String, String>) adapterView.getItemAtPosition(i);

                targetNum = map.get("Phone").toString();
                targetNumFinal = targetNum.trim();
                targetNumFinal = targetNumFinal.trim();
                targetNumFinal = targetNumFinal.replace(" ", "");
                targetNumFinal = targetNumFinal.replaceAll("[^0-9]","");

                if((isCountryCodeAvail) && (checkBoxCountryCode.isChecked()) && (targetNumFinal.startsWith(countryCode))){
                    Log.v("VERBOSE_PHONE_TAG","|||| COUNTRY CODE DETECTED!!! |||||");
                    tnFinal = targetNumFinal.substring(0+countryCode.length(),targetNumFinal.length());
                }
                else{
                    tnFinal = targetNumFinal;
                }

                Log.v("VERBOSE_PHONE_TAG","|||| countryCode = "+countryCode+" |||||");
                Log.v("VERBOSE_PHONE_TAG","|||| targetNumFinal = "+tnFinal+" |||||");

                autCompTxtVwPhoneNum.setText(tnFinal);
                targetNumFinal = null;
                targetNum = null;
                countryCode = null;
            }
        });


        //['Save Preferences' Button On Click Listener]

        savePrefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();

                String verboseSavePrefTag = "VERBOSE_SAVE_PREFERENCES";

                String accessNum = autCompTxtVwAccessNum.getText().toString();
                String prefix = edtTxtPrefix.getText().toString();

                if(accessNum.trim().length() < 10)
                {
                    Toast.makeText(activity, "Please enter a valid access number!",Toast.LENGTH_LONG).show();
                    return;
                }

                else if(prefix.trim().length() < 2)
                {
                    Toast.makeText(activity, "Please enter a valid prefix!", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS", 0);
                SharedPreferences.Editor userPrefEditor = userPreferences.edit();
                userPrefEditor.putString("CALLEASY_ACCESS_NUM",accessNum);
                userPrefEditor.putString("CALLEASY_USR_PREFIX",prefix);
                userPrefEditor.commit();
                Toast.makeText(activity, "Preferences Saved! :)", Toast.LENGTH_LONG).show();
            }
        });

        //[Call Button On Click Listener]

        callButton.setOnClickListener(new View.OnClickListener() {

            Activity activity = getActivity();

            @Override
            public void onClick(View view) {

                String verbosePhoneTag = "VERBOSE_PHONE";
                boolean isCountryCodeAvail = false;
                boolean canCall = false;

                //validating Access Number
                String accessNum = autCompTxtVwAccessNum.getText().toString();
                accessNum = accessNum.trim();
                accessNum = accessNum.replace(" ","");
                accessNum.replaceAll("[^0-9]","");

                autCompTxtVwAccessNum.setText(accessNum);


                //validating PIN
                SharedPreferences userPreferences = activity.getSharedPreferences("CALLEASY_USR_PREFS",0);
                String pin = userPreferences.getString("CALLEASY_PIN", "");
                pin = pin.trim();
                pin = pin.replace(" ","");
                pin = pin.replaceAll("[^0-9]","");
                final String pinF = pin;


                //validating Prefix
                String prefix = edtTxtPrefix.getText().toString();
                prefix = prefix.trim();
                prefix = prefix.replace(" ","");
                prefix = prefix.replaceAll("[^0-9]","");
                edtTxtPrefix.setText(prefix);

                //validating Target Number
                String targetNum = autCompTxtVwPhoneNum.getText().toString();
                targetNum = targetNum.trim();
                targetNum = targetNum.replace(" ","");
                targetNum = targetNum.replaceAll("[^0-9]","");
                autCompTxtVwPhoneNum.setText(targetNum);


                if(targetNum.trim().length() < 7 && checkBoxCountryCode.isChecked()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                    alertDialogBuilder.setTitle("Please Check Target Number")
                                       .setMessage("You have chosen to remove the country code from the Target number. The length of the  Target number: "+targetNum.toString()+" after removing the country code seems a little off. Please review Target Number. Click Continue to ignore this and go ahead with the above Target Number.")
                                        .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int id){
                                                String finalPhoneNum = "";
                                                dialog.dismiss();
                                                String accessNum = autCompTxtVwAccessNum.getText().toString();
                                                String prefix = edtTxtPrefix.getText().toString();
                                                String targetNum = autCompTxtVwPhoneNum.getText().toString();
                                                if(pinF.length()>0 && checkBoxPin.isChecked() ){
                                                    finalPhoneNum = accessNum+","+pinF+",,"+prefix+targetNum;

                                                }
                                                else{
                                                    finalPhoneNum = accessNum+",,"+prefix+targetNum;
                                                }


                                                Log.v("VERBOSE_PHONE_TAG","*****CALLING			=		"+finalPhoneNum+"*****");

                                                Intent callEasyPhoneCallIntent = new Intent(Intent.ACTION_CALL);
                                                if(finalPhoneNum.length()>0){
                                                    callEasyPhoneCallIntent.setData(Uri.parse("tel:" + finalPhoneNum));
                                                    startActivity(callEasyPhoneCallIntent);
                                                }
                                                else{
                                                    Toast.makeText(activity, "Unkonwn error!!!", Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                            }
                                        })
                                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id){
                                                dialog.dismiss();
                                                return;
                                            }
                                        })
                                        .show();
                }
                else{
                    String accessNumFinal = autCompTxtVwAccessNum.getText().toString();
                    String prefixFinal = edtTxtPrefix.getText().toString();
                    String targetNumFinal = autCompTxtVwPhoneNum.getText().toString();
                    String finalPhoneNum = "";
                    if(pinF.length() > 0 && checkBoxPin.isChecked()){
                        finalPhoneNum = accessNum+","+pinF+",,"+prefix+targetNum;
                    }
                    else{
                        finalPhoneNum = accessNumFinal+",,"+prefixFinal+targetNumFinal;
                    }

                    Log.v("VERBOSE_PHONE_TAG","*****CALLING			=		"+finalPhoneNum+"*****");

                    Intent callEasyPhoneCallIntent = new Intent(Intent.ACTION_CALL);
                    if(finalPhoneNum.length() > 0){
                        callEasyPhoneCallIntent.setData(Uri.parse("tel:" + finalPhoneNum));
                        startActivity(callEasyPhoneCallIntent);
                    }
                    else{
                        Toast.makeText(activity, "Unkonwn error!!!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        });

        return simpleCallFragment;
    }


    //******************************************************************HELPER_FUNCTIONS******************************************************************
    private void populateContactList(){

        long start = System.currentTimeMillis();
        String verbosePopContTag = "VERBOSE_POPULATE_PHONELIST";

        //Cursor allContacts = this.getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        CursorLoader allContactsLoader = new CursorLoader(this.getActivity(), ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Cursor allContacts = (Cursor)allContactsLoader.loadInBackground();

        int numOfConts = 0;

        contactList.clear();

        while(allContacts.moveToNext()){
            String contactName = allContacts.getString(allContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = allContacts.getString(allContacts.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhoneNums = allContacts.getString(allContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if(Integer.parseInt(hasPhoneNums)>0){

                //String[] query = "ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ = "+contactId;
                //CursorLoader numberLoader = new CursorLoader(this.getActivity(), ContactsContract.Contacts.CONTENT_URI,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = "+contactId, null, null, null);
                //Cursor allPhoneNums = (Cursor)numberLoader.loadInBackground();

                Cursor allPhoneNums = this.getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = "+contactId,null,null);
                while(allPhoneNums.moveToNext()){
                    String contactPhoneNumber = allPhoneNums.getString(allPhoneNums.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Map<String,String> namePhone = new HashMap<String, String>();
                    namePhone.put("Name", contactName);
                    namePhone.put("Phone",contactPhoneNumber);
                    contactList.add(namePhone);
                }
                allPhoneNums.close();
            }
            numOfConts++;
        }
        allContacts.close();

        //DEBUGGING
        long end = System.currentTimeMillis();
        float elapsedTimeSec = (end - start)/1000F;
        Log.v(verbosePopContTag,"no. of secs = "+elapsedTimeSec);
        Log.v(verbosePopContTag,"no. of contacts loaded = "+numOfConts);
    }


    public void savePreferences(View view){

        String verboseSavePrefTag = "VERBOSE_SAVE_PREFERENCES";

        String accessNum = autCompTxtVwAccessNum.getText().toString();
        String prefix = edtTxtPrefix.getText().toString();

        if(accessNum.trim().length() < 10){
            Toast.makeText(this.getActivity(), "Please enter a valid access number!",Toast.LENGTH_LONG).show();
            Log.v(verboseSavePrefTag,"*****Access number entered = "+accessNum+"*****");
            return;
        }

        else if(prefix.trim().length() < 2){
            Toast.makeText(this.getActivity(), "Please enter a valid prefix!", Toast.LENGTH_LONG).show();
            Log.v(verboseSavePrefTag,"*****Prefix entered = "+prefix+"*****");
            return;
        }

        SharedPreferences userPreferences = this.getActivity().getSharedPreferences("CALLEASY_USR_PREFS", 0);
        SharedPreferences.Editor userPrefEditor = userPreferences.edit();
        userPrefEditor.putString("CALLEASY_ACCESS_NUM",accessNum);
        userPrefEditor.putString("CALLEASY_USR_PREFIX",prefix);
        userPrefEditor.commit();
        Toast.makeText(this.getActivity(), "Preferences Saved!", Toast.LENGTH_LONG).show();

    }
}
