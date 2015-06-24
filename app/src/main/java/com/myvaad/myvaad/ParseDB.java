package com.myvaad.myvaad;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.net.ParseException;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.ParseException;

import dialogs.RingProgressDialog;

import android.support.annotation.StringRes;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

/**
 * @author shlomi fresko 25.3.15
 *         Last UPDAtE-22/4/15 includes daniel & itai additions
 *         index of methods:
 *         -----------------------------------------------------------------------------------------
 *         signUpUser(String userName,String password,String email,String FamilyName,Bitmap picture )
 *         This method signUp user to app and return message if there is a problem(user name taken or email...)
 *         getting userName password email FamilyName picturePath buildingCode
 *         -------------------------------------------------------------------------------
 *         private byte[] convertImageToByteArray(Bitmap bitmap)
 *         this method convert Bitmap to byte array-->parse can store files only in byte[] 10M
 *         --------------------------------------------------------------------------------
 *         protected void LogInUser(String userName,String password)
 *         -------------------------------------------------------------------------------
 *         protected void logOutUser()
 *         --------------------------------------------------------------------------------
 *         protected void resetUserPassword(String email)
 *         This method reset user password
 *         This will attempt to match the given email with the user's email or username field,
 *         and will send them a password reset email.
 *         The flow for password reset is as follows:
 *         1.User requests that their password be reset by typing in their email.
 *         2.Parse sends an email to their address, with a special password reset link.
 *         3.User clicks on the reset link, and is directed to a special Parse page that will allow them type in a new password.
 *         4.User types in a new password. Their password has now been reset to a value they specify.
 *         -----------------------------------------------------------------------------------
 *         protected boolean isUserSignIn()
 *         This method check any user signed in --> return true if there is a user signed in
 *         --------------------------------------------------------------------------------
 *         protected ParseUser getcurrentUser()
 *         This method return current ParseUser(all the object)
 *         ---------------------------------------------------------------------------------
 *         protected String getcurrentUserName()
 *         This method return current user userName-->return String
 *         ----------------------------------------------------------------------------------
 *         protected Bitmap getcurrentUserPicture()
 *         This method return current user Picture in Bitmap
 *         -----------------------------------------------------------------------------------
 *         private Bitmap getUserPicture(ParseUser user)
 *         This method return user Picture in Bitmap
 *         ------------------------------------------------------------------------------------
 *         private Bitmap parseFileToBitmap(ParseFile file)
 *         This method return Bitmap from ParseFile
 *         ------------------------------------------------------------------------------------
 *         protected String getcurrentUserFamilyName()
 *         This method return current user FamilyName-->return String
 *         ---------------------------------------------------------------------------------
 *         private String getUserFamilyName(ParseUser user)
 *         This method return current user FamilyName
 *         -------------------------------------------------------------------------------
 *         protected String getCurrentUserBuildingCode()
 *         This method getting Current User BuildingCode-->return String
 *         ---------------------------------------------------------------------
 *         protected boolean isUserExists(String userName)
 *         This method return true if user exists in Parse dataBase and false if not
 *         -----------------------------------------------------------------------------------
 *         protected boolean isUserAdmin(String userName)
 *         This method return true if user is Admin--> "isAdmin" field=true and false if not
 *         -------------------------------------------------------------------------------------
 *         protected boolean isBuildingCodeExists(String buildingCode)
 *         This method return true if BuildingCode exists in buildings class(table) and false if not
 *         ------------------------------------------------------------------------------------
 *         protected String getBuildingAdrees(String buildingCode)
 *         This method return buildingAdress by getting buildingCode
 *         ------------------------------------------------------------------------------------
 *         protected void updateUserBuildingCode(String buildingCode)
 *         This method adding buildingCode to currentUser-->used when user signUp for building
 *         -----------------------------------------------------------------------------------
 *         protected void signUpBuilding(String buildingCode,String address,String paypalEmail)
 *         This method sign up new building in class(table)buildings-->created by building Admin
 *         ------------------------------------------------------------------------------------
 *         protected void signUpBuildingWithoutPaypal(String buildingCode,String address)
 *         This method sign up new building in class(table)buildings without paypal account
 *         ------------------------------------------------------------------------------------
 *         protected void updatePaypalToBuilding(String paypalEmail)
 *         This method updating paypal account to Building that dosn't have one.
 *         The new paypal account update to current user building
 *         **********Need to make sure we signUp user Correctly with correct BuildingCode!!!*******
 *         --------------------------------------------------------------------------------------
 *         protected void updateNoticeBoard(String noticeContent)
 *         This method updating new notice in class(table) noticeBoard
 *         updating the user that updated the notice automatically from current user
 *         updating user buildingCode from current user
 *         ------------------------------------------------------------------------------------
 *         protected void deleteNotice(String ObjectId)
 *         This method deleting message from "noticeBoard" by getting ObjectId
 *         This method let user delete this message only if this is his message or if is a admin!!!
 *         if user can't delete the message --> sending toast
 *         ------------------------------------------------------------------------------------------
 *         protected List getCurrentUserNoticeBoard()
 *         this method return a List of notices of current user building.
 *         every sell contain a notice(List).
 *         The notice List contain a ObjectId,noticeContent,noticeCreateTime,familyName,user picture(Bitmap)
 *         -------------------------------------------------------------------------------------
 *         protected void updateNewfailure(String failureTitle,String failureContent )
 *         This method updating new failure in class(table) failures-->will contain all failures
 *         of all buildings
 *         The bid field will updated when admin will update it-->default is o
 *         The approvedBy field will updated when users will approve the failure bid
 *         The status field will update when bit will update,and when failure will closed
 *         ------------------------------------------------------------------------------------
 *         protected void updateFailureBid(String newBid,String failureObjectId)
 *         This method updating new Failure Bid by getting newBid and failureObjectId
 *         updating status to "������" from string.xml name=treating
 *         -------------------------------------------------------------------------
 *         protected void updateFailureApprovedByCurrentUser(String failureObjectId)
 *         This method updating the current user approve ,for admin bid to open failure.
 *         updating approvedBy field with the updated usersList in failures class(table).
 *         -----------------------------------------------------------------------------
 *         protected void updateFailureApprovedBy(ParseUser user,String failureObjectId)
 *         This method adding the user that approve the admin bid to open failure to List of users that
 *         in approvedBy field
 */
public class ParseDB {
    private Context context;
    private List NoticeBoardList = new ArrayList();

    //singleton instance container
    private static ParseDB instance;

    private ParseDB(Context context) {
        this.context = context;
    }

    //static method to initilaze the instance to One-single Object(instance)from this class
    public static ParseDB getInstance(Context context) {
        if (instance == null) instance = new ParseDB(context);
        return instance;
    }
    //need to update R.string.treating (����-->������) to use method updateFailureBid!!!

    //To use Parse, don't forget to add 2 permissions in manifest for INTERNET & ACCESS_NETWORK_STATE
    //AND dependency libraries

    //This method signUp user to app and return message if there is a problem(user name taken or email...)
    //getting userName password email familyName pictureBitmap buildingCode
    protected void signUpUser(String userName, String password, String email, String familyName, Bitmap picture, final Context con) {
        final RingProgressDialog dialog = new RingProgressDialog(con);

        final ParseUser user = new ParseUser();
        //parse build setters
        user.setUsername(userName);
        user.setPassword(password);
        user.setEmail(email);

        ParseACL ACL = new ParseACL();
        // Optionally enable public read access while disabling public write access.
        ACL.setPublicReadAccess(true);
        user.setACL(ACL);

        // other fields can be set just like with ParseObject by put
        user.put("isAdmin", false);
        user.put("familyName", familyName);

        //convertImageToByteArray() convert image to byte array
        //Parse get file only as byte array with max size of 10Mbyte
        byte[] image = convertImageToByteArray(picture);

        // Create the ParseFile
        final ParseFile file = new ParseFile(userName + ".png", image);
        // Upload the image into Parse Cloud
        //file.saveInBackground();

        file.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Create a column named "picture" and insert the image when parse file saved
                    user.put("picture", file);
                    //The signUp will done after file will be save
                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // user signUp Succeeded --> Let them use the app now.
                                Toast.makeText(context, R.string.signup_succeed, Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(context, MainLoginScreen.class);
                                context.startActivity(i);
                                ((Activity) con).finish();
                                dialog.dismiss();
                            } else {
                                // Sign up didn't succeed. Look at the ParseException
                                // to figure out what went wrong
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                Log.i("***Parse Exception****", e.getLocalizedMessage());
                                dialog.dismiss();
                            }
                        }
                    });
                } else {

                }
            }
        });
    }
    //This method convert Image(bitmap) To ByteArray

    private byte[] convertImageToByteArray(Bitmap bitmap) {
        byte[] byteImage = null;
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.androidbegin);
        //Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteImage = stream.toByteArray();

        return byteImage;
    }

    //This method logIn user
    protected void LogInUser(String userName, String password, final Context con) {
        final RingProgressDialog dialog = new RingProgressDialog(con);
        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    if (getCurrentUserBuildingCode() == null) {
                        Intent i = new Intent(context, NotInBuilding.class);
                        context.startActivity(i);
                        dialog.dismiss();
                        ((Activity) con).finish();

                    } else {
                        Intent i = new Intent(context, MainActivity.class);
                        context.startActivity(i);
                        dialog.dismiss();
                        ((Activity) con).finish();
                    }
                } else {
                    // SignUp failed. Look at the ParseException to see what happened.
                    dialog.dismiss();
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("***Parse Exception****", e.getLocalizedMessage());
                }
            }
        });
    }

    protected void logOutUser(Context con) {
        ParseUser.logOut();
        if (!isUserSignIn()) {
            Intent i = new Intent(context, MainLoginScreen.class);
            context.startActivity(i);
            ((Activity) con).finish();
        } else {
            Toast.makeText(context, "***ERROR***", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * This method reset user password
     * This will attempt to match the given email with the user's email or username field,
     * and will send them a password reset email.
       The flow for password reset is as follows:
       1.User requests that their password be reset by typing in their email.
       2.Parse sends an email to their address, with a special password reset link.
       3.User clicks on the reset link, and is directed to a special Parse page that will allow them type in a new password.
       4.User types in a new password. Their password has now been reset to a value they specify.
     */
    protected void resetUserPassword(String email) {
        ParseUser.requestPasswordResetInBackground(email);
    }

    //This method check any user signed in --> return true if there is a user signed in
    protected boolean isUserSignIn() {
        //THIS if call getcurrentUser() method and check if user sign in
        if (getcurrentUser() != null) {
            // Can do stuff with the currentUser
            return true;
        } else {
            //No user signed in to app
            // show the signUp or login screen
            return false;
        }
    }

    //This method return current ParseUser(all the object)
    protected ParseUser getcurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Return currentUser -->Can do stuff with the currentUser
            return currentUser;
        } else {
            //No user signed in to app
            // show the signUp or login screen
            return null;
        }
    }

    //********************Itai new 11/6/15
    //This method return current User Object Id
    protected String getCurrentUserObjectId() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Return currentUser -->Can do stuff with the currentUser
            return currentUser.getObjectId();
        } else {
            //No user signed in to app
            // show the signUp or login screen
            return null;
        }
    }

    //This method return current user userName
    protected String getcurrentUserName() {
        ParseUser currentUser = getcurrentUser();
        if (currentUser != null) {
            return currentUser.getUsername();
        } else return null;
    }

    //This method return current user Picture in Bitmap
    protected Bitmap getcurrentUserPicture() {
        ParseUser currentUser = getcurrentUser();
        if (currentUser != null) {
            ParseFile file = currentUser.getParseFile("picture");
            return parseFileToBitmap(file);
        } else return null;
    }

    //This method return user Picture in Bitmap
    private Bitmap getUserPicture(ParseUser user) {
        ParseFile file = user.getParseFile("picture");
        return parseFileToBitmap(file);
    }

    //This method return Bitmap from ParseFile
    protected Bitmap parseFileToBitmap(ParseFile file) {
        byte[] bitmapdata = null;
        try {
            bitmapdata = file.getData();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
        //this is Bitmap
        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
    }

    //This method return current user FamilyName
    protected String getcurrentUserFamilyName() {
        ParseUser currentUser = getcurrentUser();
        if (currentUser != null) {
            return currentUser.getString("familyName");
        } else return null;
    }

    //This method return user FamilyName
    protected String getUserFamilyName(ParseUser user) {
        return user.getString("familyName");
    }


    //This method getting Current User BuildingCode-->return String
    protected String getCurrentUserBuildingCode() {
        ParseUser currentUser = getcurrentUser();
        String currentUserBuildingCode = currentUser.getString("buildingCode");
        return currentUserBuildingCode;
    }


    //This method return true if user exists in Parse dataBase and false if not
    protected boolean isUserExists(String userName) {
        boolean userExists = false;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            query.whereEqualTo("username", userName);
            List<ParseUser> objects = query.find();
            if (objects.get(0).getUsername() != null) userExists = true;
        } catch (Exception e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return userExists;
    }

    //This method return true if user is Admin--> "isAdmin" field=true and false if not
    protected boolean isCurrentUserAdmin() {
        boolean userisAdmin = false;
        ParseUser currentUser = getcurrentUser();
        userisAdmin = currentUser.getBoolean("isAdmin");
        return userisAdmin;
    }

    //This method return true if BuildingCode exists in buildings class(table) and false if not
    protected boolean isBuildingCodeExists(String buildingCode) {
        boolean BuildingCodeExists = false;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        try {
            query.whereEqualTo("buildingCode", buildingCode);
            ParseObject building = query.find().get(0);
            if (building != null) BuildingCodeExists = true;
        } catch (Exception e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return BuildingCodeExists;
    }

    //This method return buildingAdress by getting buildingCode
    protected String getBuildingAdrees(String buildingCode) {
        String buildingAdress = null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        try {
            query.whereEqualTo("buildingCode", buildingCode);
            ParseObject building = query.find().get(0);
            if (building != null) {
                buildingAdress = building.getString("address");
            }
        } catch (Exception e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return buildingAdress;
    }

    //This method adding buildingCode to currentUser-->used when user signUp for building
    protected void updateUserBuildingCode(String buildingCode) {
        ParseUser currentUser = getcurrentUser();
        currentUser.put("buildingCode", buildingCode);
        currentUser.saveInBackground();
    }

    //**********************UPDATED BY DANIEL
    //This method update familyname to currentUser--
    protected void updateUserFamilyName(String familyName) {
        ParseUser currentUser = getcurrentUser();
        currentUser.put("familyName", familyName);
        currentUser.saveInBackground();
    }

    //This method sign up new building in class(table)buildings-->created by building Admin
    protected void signUpBuilding(String buildingCode, String address, String paypalEmail, String numberOfHouses) {
        //The user that sign Up the Building is the admin
        ParseUser currentUser = getcurrentUser();
        currentUser.put("isAdmin", true);
        //the admin that create the building add automatically to the building
        updateUserBuildingCode(buildingCode);
        ParseObject building = new ParseObject("buildings");
        building.put("buildingCode", buildingCode);
        building.put("address", address);
        building.put("paypal", paypalEmail);
        building.put("houses", numberOfHouses);
        building.saveInBackground();
    }

    //This method sign up new building in class(table)buildings without paypal account
    protected void signUpBuildingWithoutPaypal(String buildingCode, String address, String numberOfHouses) {
        //The user that sign Up the Building is the admin
        ParseUser currentUser = getcurrentUser();
        currentUser.put("isAdmin", true);
        //the admin that create the building add automatically to the building/
        updateUserBuildingCode(buildingCode);
        ParseObject building = new ParseObject("buildings");
        building.put("buildingCode", buildingCode);
        building.put("address", address);
        building.put("houses", numberOfHouses);
        building.saveInBackground();
    }

    //This method updating paypal account to Building that not have one.
    //The new paypal account update to current user building
    //***********Need to make sure we signUp user Correctly with correct BuildingCode!!!*******
    protected void updatePaypalToBuilding(String paypalEmail) {
        //using our method getCurrentUserBuildingCode()
        String currentUserBuildingCode = getCurrentUserBuildingCode();
        //getting all "buildings" table
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        //Query Constraints
        query.whereContains("buildingCode", currentUserBuildingCode);
        ParseObject currentUserBuilding;
        try {
            //finding the current user building and updating the paypal field
            currentUserBuilding = query.find().get(0);
            currentUserBuilding.put("paypal", paypalEmail);
            currentUserBuilding.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
    }


    //This method updating new notice in class(table) noticeBoard
    //updating the user that updated the notice automatically from current user
    //updating user buildingCode from current user
    protected void updateNoticeBoard(String noticeContent) {
        ParseObject notice = new ParseObject("noticeBoard");
        ParseUser currentUser = getcurrentUser();
        //Get current user from the method getcurrentUser() and put him in a new field
        notice.put("user", currentUser);
        notice.put("userFamilyName", getcurrentUserFamilyName());
        notice.put("userPic", currentUser.getParseFile("picture"));
        notice.put("content", noticeContent);
        //get current user buildingCode and put it in new field
        notice.put("buildingCode", currentUser.getString("buildingCode"));
        notice.saveInBackground();
    }

    //**************************updated by ITAI 20/4
    //This method deleting message from "noticeBoard" by getting ObjectId
    //This method let user delete this message only if this is his message or if is a admin!!!
    //if user can't delete the message --> sending toast
    protected void deleteNotice(String ObjectId) {
        ParseUser currentUser = getcurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
        try {
            //Getting notice object(table row)
            ParseObject notice = query.get(ObjectId);
            if (notice != null) {
                //The notice created by currentUser or currentUser is Admin
                //if(notice.getParseUser("user")==currentUser || isCurrentUserAdmin()){
                //deleting notice
                notice.deleteInBackground();
                //}
                //this user can't delete this message
                //else{
                //Toast.makeText(context,context.getString(R.string.cantDeleteMessage), Toast.LENGTH_SHORT).show();
                //}
            } else Log.i("***Parse Exception****", "incorrect ObjectId entered");
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //itai new method
    protected void deleteAllNotices(String buildingCode) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
        query.whereContains("buildingCode", buildingCode);
        try {
            List<ParseObject> notices = query.find();

            for (int i = 0; i < notices.size(); i++) {
                notices.get(i).deleteInBackground();
            }


        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //itai new method
    protected void editNoticeBoard(String noticeContent, String ObjectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");

        try {
            ParseObject notice = query.get(ObjectId);
            notice.put("content", noticeContent);
            notice.saveInBackground();
        } catch (ParseException e) {

            e.printStackTrace();
        }
    }

    /*
     * This method return a List of notices of current user building.
	   every sell contain a notice(List).
	   The notice List contain a ObjectId,noticeContent,noticeCreateTime,familyName,user picture(Bitmap)
     */

/*
    protected void reloadCurrentUserNoticeBoard() {
        NoticeBoardList.clear();
        //final List outputNoticeList = new ArrayList();
        String CurrentUserBuildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
        //Query Constraints-->all the notices for current user building
        query.whereContains("buildingCode", CurrentUserBuildingCode);
        query.orderByDescending("updatedAt");

        //finding all the notices for current user building
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> notices, ParseException e) {
                if (e == null) {
                    for (ParseObject noticeRow : notices) {
                        List rowNoticeList = new ArrayList();
                        //get specific data from each row
                        String content = noticeRow.getString("content");

                        Date updatedAt = noticeRow.getUpdatedAt();
                        String noticeTime = updatedAt.toLocaleString();
                        String ObjectId = noticeRow.getObjectId();

                        String familyName = noticeRow.getString("userFamilyName");
                        ParseFile userPicture = noticeRow.getParseFile("userPic");
                        Bitmap userPic = parseFileToBitmap(userPicture);
                        //ParseUser user=noticeRow.getParseUser("user");
                        //Bitmap userPicture=(Bitmap)getUserPicture(user);
                        //String familyName=getUserFamilyName(user);

                        rowNoticeList.add(ObjectId);
                        rowNoticeList.add(content);
                        rowNoticeList.add(noticeTime);
                        rowNoticeList.add(familyName);
                        rowNoticeList.add(userPic);
                        NoticeBoardList.add(rowNoticeList);
                    }
                } else {
                    Log.e("**PARSE ERROR**", "Error: " + e.getMessage());
                }
            }
        });

    }

    public List getCurrentUserNoticeBoard(){
        //reloadCurrentUserNoticeBoard();
        Log.i("***NoticeBoard***", NoticeBoardList.size() + "");
        return  NoticeBoardList;
    }


        protected List getCurrentUserNoticeBoard(){

		List outputNoticeList= new ArrayList();
		String CurrentUserBuildingCode=getCurrentUserBuildingCode();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("noticeBoard");
		//Query Constraints-->all the notices for current user building
		query.whereContains("buildingCode", CurrentUserBuildingCode);
		query.orderByDescending("updatedAt");
		List <ParseObject> notices = null;
		try {
			//finding all the notices for current user building
			notices=query.find();
			for(ParseObject noticeRow:notices){
				List rowNoticeList= new ArrayList();
    			//get specific data from each row
				String content =noticeRow.getString("content");

    			Date updatedAt = noticeRow.getUpdatedAt();
    			String noticeTime=updatedAt.toLocaleString();
    			String ObjectId= noticeRow.getObjectId();

    			String familyName= noticeRow.getString("userFamilyName");
    			ParseFile userPicture=noticeRow.getParseFile("userPic");
    			Bitmap userPic=parseFileToBitmap(userPicture);
    			//ParseUser user=noticeRow.getParseUser("user");
    			//Bitmap userPicture=(Bitmap)getUserPicture(user);
    			//String familyName=getUserFamilyName(user);

    			rowNoticeList.add(ObjectId);
    			rowNoticeList.add(content);
    			rowNoticeList.add(noticeTime);
    			rowNoticeList.add(familyName);
    			rowNoticeList.add(userPic);
    			outputNoticeList.add(rowNoticeList);
    		}
		} catch (ParseException e) {
			e.printStackTrace();
			Log.i("***Parse Exception****", e.getLocalizedMessage());
		}
		return outputNoticeList;
	}
*/


    protected List getCurrentUserFailuresBoard() {
        List outputFailuresList = new ArrayList();
        String CurrentUserBuildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        //Query Constraints-->all the failures for current user building
        query.whereContains("buildingCode", CurrentUserBuildingCode);
        query.whereEqualTo("state", true);
        query.orderByDescending("updatedAt");
        List<ParseObject> failures = null;
        try {
            //finding all the failures for current user building
            failures = query.find();
            for (ParseObject failuresRow : failures) {
                List rowFailureList = new ArrayList();
                //get specific data from each row
                String title = failuresRow.getString("title");
                String content = failuresRow.getString("content");

                String bidValue = failuresRow.getString("bid");
                String bidPerformedBy = failuresRow.getString("performedBy");

                String status = failuresRow.getString("status");
                Date updatedAt = failuresRow.getCreatedAt();
                String noticeTime = updatedAt.toLocaleString();
                String ObjectId = failuresRow.getObjectId();

                String familyName = failuresRow.getString("userFamilyName");
                ParseFile userPicture = failuresRow.getParseFile("userPic");
                Bitmap userPic = parseFileToBitmap(userPicture);

                List<String> approvedByList = new ArrayList();
                //List of all the users that approved the bid for the repair the malfunction
                if (failuresRow.getList("approvedBy") != null) {
                    approvedByList = failuresRow.getList("approvedBy");
                } else approvedByList.add("no one approve");
                //ParseUser user=failuresRow.getParseUser("user");
                //Bitmap userPicture=getUserPicture(user);
                //String familyName=getUserFamilyName(user);

                rowFailureList.add(ObjectId);
                rowFailureList.add(title);
                rowFailureList.add(content);
                rowFailureList.add(bidValue);
                rowFailureList.add(bidPerformedBy);
                rowFailureList.add(status);
                rowFailureList.add(noticeTime);
                rowFailureList.add(familyName);
                rowFailureList.add(userPic);
                rowFailureList.add(approvedByList);
                outputFailuresList.add(rowFailureList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputFailuresList;
    }

    //This method updating new failure in class(table) failures-->will contain all failures
    // of all buildings
    protected void updateNewfailure(String failureTitle, String failureContent) {
        //The bid field will updated when admin will update it-->default is o
        //The approvedBy field will updated when users will approve the failure bid
        //The status field will update when bit will update,and when failure will closed
        ParseObject failure = new ParseObject("failures");
        ParseUser currentUser = getcurrentUser();
        failure.put("user", currentUser);
        failure.put("userFamilyName", getcurrentUserFamilyName());
        failure.put("userPic", currentUser.getParseFile("picture"));
        failure.put("buildingCode", currentUser.getString("buildingCode"));
        failure.put("title", failureTitle);
        failure.put("content", failureContent);
        failure.put("bid", "");
        failure.put("performedBy", "");
        failure.put("status", context.getString(R.string.notTreated));
        failure.put("state", true);
        failure.saveInBackground();
    }

    //****************UPDATED BY ITAI 20/4***************
    //This method updating new Failure Bid by getting newBid and failureObjectId
    //updating status to "·ËÈÙÂÏ" from string.xml name=treating
    protected void updateFailureBid(String newBid, String performedBy, String failureObjectId, boolean firstAdd) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        ParseObject failure;
        try {
            failure = query.get(failureObjectId);
            failure.put("bid", newBid);
            failure.put("performedBy", performedBy);
            failure.put("status", context.getString(R.string.treating));
            if (firstAdd)
                updateFailureApprovedByCurrentUser(failureObjectId);
            failure.saveInBackground();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //This method updating the current user approve ,for admin bid to open failure.
    //updating approvedBy field with the updated usersList in failures class(table).
    protected void updateFailureApprovedByCurrentUser(String failureObjectId) {
        updateFailureApprovedBy(getcurrentUserFamilyName(), failureObjectId);
    }

    //This method updating the user that approve the admin bid to open failure
    protected void updateFailureApprovedBy(String userFamilyName, String failureObjectId) {
        List<String> usersFamilyName = new ArrayList();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        ParseObject failure;
        try {
            //getting the failure object that need to update
            failure = query.get(failureObjectId);
            if ((failure.getList("approvedBy")) != null) {
                //the field "approvedBy" is not empty-->adding one more user that approve
                usersFamilyName = failure.getList("approvedBy");
                usersFamilyName.add(userFamilyName);
            } else {
                //the field "approvedBy" is empty-->initialize it with first user that approve
                usersFamilyName.add(userFamilyName);
            }
            failure.put("approvedBy", usersFamilyName);
            failure.saveInBackground();

        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //*************ITAI NEW 20/4
    //delete a failure
    protected void deleteFailure(String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        try {
            //Getting notice object(table row)
            ParseObject failure = query.get(objectId);
            if (failure != null) {
                failure.deleteInBackground();
            } else Log.i("***Parse Exception****", "incorrect ObjectId entered");
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    //*******************NEW ITAI 20/4
    //removes approve of the user
    protected void removeFailureApprovedBy(String userFamilyName, String failureObjectId) {
        List<String> usersFamilyName = new ArrayList();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        ParseObject failure;
        try {
            //getting the failure object that need to update
            failure = query.get(failureObjectId);
            if ((failure.getList("approvedBy")) != null) {
                //the field "approvedBy" is not empty-->adding one more user that approve
                usersFamilyName = failure.getList("approvedBy");
                usersFamilyName.remove(userFamilyName);
            } else {
            }
            failure.put("approvedBy", usersFamilyName);
            failure.saveInBackground();

        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    /**
     * UPDATE OF Daniel to get user ObjectId String *
     */
    //***********************NEW ITAI 22/4
    //get users list from building except for admin
    protected List getUsersList() {
        List outputUsersList = new ArrayList();
        String buildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereNotEqualTo("username", getcurrentUserName());
        query.addAscendingOrder("familyName");
        List<ParseObject> users = null;
        try {
            //finding all users for current user building
            users = query.find();
            for (ParseObject usersRow : users) {
                List rowUserList = new ArrayList();
                //get specific data from each row
                String familyName = usersRow.getString("familyName");
                String userObjectId = usersRow.getObjectId().toString();
                ParseFile userPicture = usersRow.getParseFile("picture");
                Bitmap userPic = parseFileToBitmap(userPicture);
                rowUserList.add(familyName); //0
                rowUserList.add(userPic);     //1
                rowUserList.add(userObjectId); //2
                outputUsersList.add(rowUserList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputUsersList;
    }

    //**************************Itai new 9/6/15
    //get payments list
    protected List getPayments() {
        List outputPaymentsList = new ArrayList();
        String buildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereContains("paymentType", "regular");
        query.addDescendingOrder("createdAt");
        List<ParseObject> payments = null;
        try {
            //finding all payments for current user building
            payments = query.find();
            for (ParseObject paymentRow : payments) {
                List rowPaymentList = new ArrayList();
                //get specific data from each row
                String paymentName = paymentRow.getString("description");
                String amount = paymentRow.getString("amount");
                Date date = paymentRow.getCreatedAt();
                //SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                String formatedDate = date.toLocaleString();
                String objectId = paymentRow.getObjectId();
                rowPaymentList.add(paymentName); //0
                rowPaymentList.add(amount); //1
                //rowPaymentList.add(dt.format(date)); //2
                rowPaymentList.add(formatedDate); //2
                rowPaymentList.add(objectId); //3
                outputPaymentsList.add(rowPaymentList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputPaymentsList;
    }

    //**************************Itai new 9/6/15
    //get users list
    protected List getSimpleUsersList() {
        List outputUsersList = new ArrayList();
        String buildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereNotEqualTo("username", getcurrentUserName());
        query.addAscendingOrder("familyName");
        List<ParseObject> users = null;
        try {
            //finding all users for current user building
            users = query.find();
            for (ParseObject usersRow : users) {
                List rowUserList = new ArrayList();
                //get specific data from each row
                String familyName = usersRow.getString("familyName");
                String userObjectId = usersRow.getObjectId();
                rowUserList.add(familyName); //0
                rowUserList.add(userObjectId); //1
                outputUsersList.add(rowUserList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputUsersList;
    }

    //**************Itai new 9/6/15
    //get paid users list
    protected List getPaidUsersForPayment(String objectId) {
        List usersList = getSimpleUsersList();
        List<String> paidByUserList = new ArrayList();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        ParseObject payment;
        try {
            payment = query.get(objectId);
            if ((payment.getList("paidBy")) != null) {
                paidByUserList = (payment.getList("paidBy"));
                for (int i = 0; i < usersList.size(); i++) {
                    List tmpList = (List) usersList.get(i);
                    String famName = "" + tmpList.get(0);
                    String uObjectId = "" + tmpList.get(1);
                    List<Object> myList = new ArrayList<Object>();
                    myList.add(famName);
                    myList.add(uObjectId);
                    if (paidByUserList.contains(uObjectId)) {
                        myList.add(true);
                        usersList.set(i, myList);
                    } else {
                        myList.add(false);
                        usersList.set(i, myList);
                    }
                }
            } else {
                for (int i = 0; i < usersList.size(); i++) {
                    List tmpList = (List) usersList.get(i);
                    String famName = "" + tmpList.get(0);
                    String uObjectId = "" + tmpList.get(1);
                    List<Object> myList = new ArrayList<Object>();
                    myList.add(famName);
                    myList.add(uObjectId);
                    myList.add(false);
                    usersList.set(i, myList);
                }
            }
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return usersList;
    }

    //*************Itai new 10/6/15
    //set paid for user
    protected void addPaidUserToPaymentList(String objectId, String uObjectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        ParseObject payment;
        try {
            //getting the payment object that need to update
            payment = query.get(objectId);
            payment.addUnique("paidBy", uObjectId);
            payment.saveInBackground();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //*************Itai new 10/6/15
    //set unpaid foruser
    protected void removePaidUserToPaymentList(String objectId, String uObjectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        ParseObject payment;
        try {
            //getting the payment object that need to update
            payment = query.get(objectId);
            payment.removeAll("paidBy", Arrays.asList(uObjectId));
            payment.saveInBackground();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //***********Itai new 10/6/15
    //get usersList for vaad bait payments
    protected List getUserListVaadBaitPayments() {
        String cBuilding = getCurrentUserBuildingCode();
        //users list object to return - paid months for each user
        List usersListObject = new ArrayList();
        //users list of building
        List usersList = getSimpleUsersList();
        //paidBy array of each payment
        List<String> paidByUserList = new ArrayList();
        //object to contain query result
        List<ParseObject> payments;
        //object to contain each row
        ParseObject payment;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //constrain to get only vaad bait payments
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("buildingCode", cBuilding);
        query.addAscendingOrder("period");
        try {
            payments = query.find();
            for (int i = 0; i < usersList.size(); i++) {
                List tmp = (List) usersList.get(i);
                String famName = "" + tmp.get(0);
                String uObjectId = "" + tmp.get(1);
                List<Boolean> paid = new ArrayList();
                List<String> objectId = new ArrayList();
                for (int j = 0; j < payments.size(); j++) {
                    payment = payments.get(j);
                    if (payment.getList("paidBy") != null) {
                        List paidList = payment.getList("paidBy");
                        if (paidList.contains(uObjectId)) {
                            paid.add(true);
                        } else {
                            paid.add(false);
                        }
                    } else {
                        paid.add(false);
                    }
                    objectId.add(payment.getObjectId());
                }
                List<Object> user = new ArrayList();
                user.add(famName); //0
                user.add(uObjectId); //1
                user.add(paid); //2
                user.add(objectId); //3
                usersListObject.add(user);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return usersListObject;
    }


    //*************Itai new 10/6/15
    //remove vaad bait for user
    protected void removePaidUserToVaadBaitPaymentList(String objectId, String uObjectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        ParseObject payment;
        try {
            //getting the payment object that need to update
            payment = query.get(objectId);
            payment.removeAll("paidBy", Arrays.asList(uObjectId));
            payment.saveInBackground();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //*************Itai new 10/6/15
    //set vaad bait for user
    protected void addPaidAllToUserVaadBaitPaymentList(String uObjectId) {
        String cBuilding = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        List<ParseObject> payments;
        ParseObject payment;
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("buildingCode", cBuilding);
        try {
            payments = query.find();
            for (int i = 0; i < payments.size(); i++) {
                payment = payments.get(i);
                payment.addUnique("paidBy", uObjectId);
                payment.saveInBackground();
            }
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    //**************Itai new 11/6/15
    protected List getPaymentsForUser() {
        String uObjectId = getCurrentUserObjectId();
        List outputPaymentsList = new ArrayList();
        String buildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereContains("paymentType", "regular");
        query.whereNotEqualTo("paidBy", uObjectId);
        query.addDescendingOrder("createdAt");
        List<ParseObject> payments = null;
        try {
            //finding all payments for current user
            payments = query.find();
            for (ParseObject paymentRow : payments) {
                List rowPaymentList = new ArrayList();
                //get specific data from each row
                String paymentName = paymentRow.getString("description");
                String amount = paymentRow.getString("amount");
                Date date = paymentRow.getCreatedAt();
                //SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                String formattedDate = date.toLocaleString();
                String objectId = paymentRow.getObjectId();
                rowPaymentList.add(paymentName); //0
                rowPaymentList.add(amount); //1
                //rowPaymentList.add(dt.format(date)); //2
                rowPaymentList.add(formattedDate); //2
                rowPaymentList.add(objectId); //3
                outputPaymentsList.add(rowPaymentList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputPaymentsList;
    }

    //**************Itai new 11/6/15
    protected List getVaadBaitPaymentsForUser() {
        String uObjectId = getCurrentUserObjectId();
        List outputPaymentsList = new ArrayList();
        String buildingCode = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereContains("paymentType", "vaad");
        query.whereNotEqualTo("paidBy", uObjectId);
        query.addDescendingOrder("createdAt");
        List<ParseObject> payments = null;
        try {
            //finding all payments for current user
            payments = query.find();
            for (ParseObject paymentRow : payments) {
                List rowPaymentList = new ArrayList();
                //get specific data from each row
                String paymentName = paymentRow.getString("description");
                String amount = paymentRow.getString("amount");
                String objectId = paymentRow.getObjectId();
                rowPaymentList.add(paymentName); //0
                rowPaymentList.add(amount); //1
                rowPaymentList.add(objectId); //2
                outputPaymentsList.add(rowPaymentList);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return outputPaymentsList;
    }

    //*******************Itai new 14/6/15
    //returns e-mail string of vaad admin for pay pal account
    protected String getVaadPayPalAccount() {
        String currentBuilding = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        ParseObject users;
        String email = "";
        //get users from current building
        query.whereContains("buildingCode", currentBuilding);
        //get admin of building
        query.whereEqualTo("isAdmin", true);
        try {
            //getting the users object
            users = query.getFirst();
            email = users.getString("email");
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
        return email;
    }

    //*********************Itai new 17/6/15
    //set failure to inactive
    protected void setFailureInactive(String failureObjectId) {
        String currentBuilding = getCurrentUserBuildingCode();
        String objectId = failureObjectId;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        query.whereEqualTo("buildingCode", currentBuilding);
        try {
            ParseObject failure = query.get(objectId);
            failure.put("state", false);
            failure.saveInBackground();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ;
    }

    //*********************Itai new 17/6/15
    //get failure
    protected ParseObject getFailure(String failureObjectId) {
        String currentBuilding = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        ParseObject failure = null;
        try {
            failure = query.get(failureObjectId);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return failure;
    }

    //*********************Itai new 17/6/15
    //create payment from failure
    protected void createPaymentFromFailure(String failureObjectId) {
        String currentBuilding = getCurrentUserBuildingCode();
        ParseObject failure = getFailure(failureObjectId);
        ParseObject payment;
        String paymentName = "", paymentPrice = "";
        paymentName = failure.getString("title");
        paymentPrice = failure.getString("bid");

        payment = new ParseObject("payments");
        payment.put("buildingCode", currentBuilding);
        payment.put("amount", paymentPrice);
        payment.put("description", paymentName);
        payment.put("paymentType", "regular");
        payment.put("paymentApproved", false);
        payment.saveInBackground();
    }

    protected boolean isVaadBaitPaymentsExists() {
        String currentBuilding = getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", currentBuilding);
        query.whereEqualTo("paymentType", "vaad");
        int myCount = 0;
        try {
            myCount = query.count();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (myCount > 0) ? true : false;
    }

    //*********************Itai new 17/6/15
    //creates new vaad bait payments for building
    protected void createVaadBaitPayments(String price) {
        String currentBuilding = getCurrentUserBuildingCode();
        for (int i = 1; i <= 12; i++) {
            ParseObject payment = new ParseObject("payments");
            payment.put("buildingCode", currentBuilding);
            payment.put("amount", price);
            payment.put("description", "ועד בית חודש " + i);
            String periodi = (i < 10) ? "0" + i : "" + i;
            payment.put("period", periodi);
            payment.put("paymentType", "vaad");
            payment.saveInBackground();
        }
    }

    //*************************Itai new 21/6/15
    //create new payment
    protected void createPayment(String paymentName, String paymentPrice) {
        String currentBuilding = getCurrentUserBuildingCode();
        ParseObject payment = new ParseObject("payments");
        payment.put("buildingCode", currentBuilding);
        payment.put("description", paymentName);
        payment.put("amount", paymentPrice);
        payment.put("paymentType", "regular");
        payment.put("paymentApproved", false);
        payment.saveInBackground();
    }

    /**
     * **************daniel new*********
     */
    //get users list
    protected List getSimpleUsersListByBuildingCode(String bCode) {
        List outputUsersList = new ArrayList();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", bCode);
        query.whereNotEqualTo("username", getcurrentUserName());
        query.addAscendingOrder("familyName");
        List<ParseObject> users = null;
        List rowUserList = new ArrayList();
        try {
            //finding all users for current user building
            users = query.find();
            for (ParseObject usersRow : users) {
                //get specific data from each row
                String familyName = usersRow.getString("familyName");
                String userObjectId = usersRow.getObjectId();
                rowUserList.add(userObjectId); //1
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return rowUserList;
    }

    /**
     * **************daniel new************************
     */
    //get users list
    protected void sendNotice(String paymentObjectId, String paymentDesc) {
        //Parse save user in install
        ParseInstallation.getCurrentInstallation().put("userNamePush", (getcurrentUser().getObjectId()).toString());
        ParseInstallation.getCurrentInstallation().saveInBackground();
        //Parse Test*************
        // Create our Installation query
        ParseQuery<ParseObject> getuserQuery = ParseQuery.getQuery("_Users");
        getuserQuery.whereContains("buildingCode", getCurrentUserBuildingCode());

        ParseQuery<ParseObject> buildingQuery = ParseQuery.getQuery("payments");
        buildingQuery.whereEqualTo("buildingCode", getCurrentUserBuildingCode());
        ParseQuery<ParseObject> descQuery = ParseQuery.getQuery("payments");
        descQuery.whereEqualTo("objectId", paymentObjectId);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(buildingQuery);
        queries.add(descQuery);

        ParseQuery<ParseObject> superQuery = ParseQuery.getQuery("payments");
        superQuery.whereContains("objectId", paymentObjectId);
        superQuery.whereContains("buildingCode", getCurrentUserBuildingCode());

        List<ParseObject> al;
        List<ParseObject> usersList = null;

        List rowUserList = new ArrayList();
        rowUserList = getSimpleUsersListByBuildingCode(getCurrentUserBuildingCode());

        List<String> rowUserList222 = new ArrayList();

        try {
            ParseObject building = superQuery.find().get(0);
            al = building.getList("paidBy");
            rowUserList.removeAll(al);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context, "Shitttttttttttt", Toast.LENGTH_LONG).show();
        }

        for (Object xx : rowUserList) {
            rowUserList222.add(xx.toString());
        }

        ParseQuery query = ParseInstallation.getQuery();

        List<String> userss = new ArrayList<String>();
        for (Object obj : rowUserList) {
            String x = obj.toString();
            Toast.makeText(context, x, Toast.LENGTH_LONG).show();
            userss.add(x);
        }
        query.whereContainedIn("userNamePush", userss);
        ParsePush androidPush = new ParsePush();
        androidPush.setMessage("?? ?????? ????? ???? " + paymentDesc);
        androidPush.setQuery(query);
        androidPush.sendInBackground();
    }

    //get Current User Building Total Expenses ******Shlomi new 21/6/15******

    protected int getCurrentUserTotalExpenses() {
        int totalExpensesAmount = 0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereEqualTo("buildingCode", getCurrentUserBuildingCode());
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("paidBy", getCurrentUserObjectId());
        query.whereEqualTo("paymentApproved", true);
        query.orderByDescending("createdAt");
        List<ParseObject> payments = null;
        try {
            //finding all payments for current user
            payments = query.find();
            for (ParseObject paymentRow : payments) {

                //get specific data from each row
                String amount = paymentRow.getString("amount");
                totalExpensesAmount += Integer.parseInt(amount);

            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception***", e.getLocalizedMessage());
        }

        return totalExpensesAmount;
    }

    //get Current User Building Total Expenses ******Shlomi new 21/6/15******

    protected int getCurrentUserBuildingTotalExpenses() {
        int totalExpensesAmount = 0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereEqualTo("buildingCode", getCurrentUserBuildingCode());
        query.whereEqualTo("paymentType", "regular");
        //query.whereEqualTo("paymentApproved", true);
        query.orderByDescending("createdAt");
        List<ParseObject> payments = null;
        try {
            //finding all payments for current user
            payments = query.find();
            for (ParseObject paymentRow : payments) {

                //get specific data from each row
                String amount = paymentRow.getString("amount");
                totalExpensesAmount += Integer.parseInt(amount);

            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("***Parse Exception***s", e.getLocalizedMessage());
        }

        return totalExpensesAmount;
    }

    /** itai new*/
    //add new user to userlist
    protected void addUser(String familyName, String apartmentNumber){
        String currentBuilding = getCurrentUserBuildingCode();
        ParseObject user = new ParseObject("_User");
        user.put("buildingCode", currentBuilding);
        user.put("description", familyName);
        user.put("amount", apartmentNumber);
        user.saveInBackground();
    }

}
