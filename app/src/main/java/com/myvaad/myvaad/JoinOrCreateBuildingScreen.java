package com.myvaad.myvaad;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class JoinOrCreateBuildingScreen extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//before the content is loading, an outer thread checks if the user is linked to a building
		//if connected, moves to the relvant screen
		//if not, loads the content of this screen
		setContentView(R.layout.join_or_create_building_screen);
	}
	
	public void join(View v){
		
	}
	
	public void create(View v){
		
	}
	


}
