package com.cookietracker.common.data

class Exceptions {

}
class FindEmptyIdException extends Exception("The object should have an id")

class InsertWithIdException extends Exception("The object insert should not have an id")