package com.tallbigup.android.cloud;

import com.avos.avoscloud.AVUser;

public class Player extends AVUser{
	
	public Player(){
		
	}
	
	public void setNickName(String nickName){
		this.put("nickName", nickName);
	}
	
	public String getNickName(){
		return this.getString("nickName");
	}
	
	public void setIMSI(String IMSI){
		this.put("IMSI", IMSI);
	}
	
	public String getIMSI(){
		return this.getString("IMSI");
	}
	
	public void setEnterId(String enterId){
		this.put("enterId", enterId);
	}
	
	public String getEnterId(){
		return this.getString("enterId");
	}
	
	public void setGameVersionCode(String gameVersionCode){
		this.put("gameVersionCode", gameVersionCode);
	}
	
	public String getGameVersionCode(){
		return this.getString("gameVersionCode");
	}
	
	public void setMoney(int money){
		this.put("money", money);
	}
	
	public int getMoney(){
		return this.getInt("money");
	}
	
	public void setPayMoney(int payMoney){
		this.put("payMoney", payMoney);
	}
	
	public int getPayMoney(){
		return this.getInt("payMoney");
	}
	
	public void setScore(int score){
		this.put("score", score);
	}
	
	public int getScore(){
		return this.getInt("score");
	}
	
	public void setLevel(String level){
		this.put("level", level);
	}
	
	public String getLevel(){
		return this.getString("level");
	}
}
