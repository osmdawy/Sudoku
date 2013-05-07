package com.example.sudoku;

public class Point {
	int x;
	int y;
	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
		
	
    @Override
    public boolean equals(Object object)
    {
    	Point p = (Point)object;
		if(this.x == p.x && this.y == p.y)
			return true;
		else return false;
	}



}
