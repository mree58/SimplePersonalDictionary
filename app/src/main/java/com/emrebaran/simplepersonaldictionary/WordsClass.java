package com.emrebaran.simplepersonaldictionary;

/**
 * Created by mree on 12.11.2016.
 */

public class WordsClass {

    //private variables
    int _id;
    String _word;
    String _explanation;

    // Empty constructor
    public WordsClass(){

    }
    // constructor
    public WordsClass(int id, String word, String explanation){
        this._id = id;
        this._word = word;
        this._explanation = explanation;
    }

    public WordsClass(String word, String explanation){
        this._word = word;
        this._explanation = explanation;
    }




    public int getID(){
        return this._id;
    }
    public void setID(int id){
        this._id = id;
    }

    public String getWord(){
        return this._word;
    }
    public void setWord(String word){
        this._word = word;
    }

    public String getExplanation(){
        return this._explanation;
    }
    public void setExplanation(String explanation){
        this._explanation = explanation;
    }
}