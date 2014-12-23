package com.reading.trackitparenttabbed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class ConvexHull {

	private ArrayList<ConvexHullMarker> Llower = new ArrayList<ConvexHullMarker>();
	private ArrayList<ConvexHullMarker> Lupper = new ArrayList<ConvexHullMarker>();
	private ArrayList<ConvexHullMarker> convexHull = new ArrayList<ConvexHullMarker>();
	private ArrayList<ArrayList<ConvexHullMarker>> listOfLists = new ArrayList<ArrayList<ConvexHullMarker>>();
	private MapSectionFragment thisFrag;

	public ConvexHull(MapSectionFragment thisFrag) {
		this.thisFrag = thisFrag;
	}

	public void drawHull(ArrayList<ConvexHullMarker> hull, boolean sorted,
			String group_id) {
		PolylineOptions options = new PolylineOptions();

		if (!sorted) {
			hull = drawSort(hull);
		}

		for (int i = 0; i < hull.size(); i++) {
			ConvexHullMarker thisMarker = hull.get(i);
			options.add(thisMarker.getMarker().getPosition());
		}

		if (hull.size() > 0) {
			options.add(hull.get(0).getMarker().getPosition());
		}

		if (thisFrag.convexLines.containsKey(group_id)) {
			Polyline deadPoly = thisFrag.convexLines.get(group_id);
			deadPoly.remove();
			thisFrag.convexLines.remove(group_id);
		}

		Polyline thisPoly = thisFrag.map.addPolyline(options.color(Color.BLUE)
				.width(2));

		thisFrag.convexLines.put(group_id, thisPoly);
	}

	// Sort the points in clockwise order so we can draw the convex hull
	private ArrayList<ConvexHullMarker> drawSort(
			ArrayList<ConvexHullMarker> hull) {

		ArrayList<ConvexHullMarker> leftSide = new ArrayList<ConvexHullMarker>();
		ArrayList<ConvexHullMarker> rightSide = new ArrayList<ConvexHullMarker>();
		ConvexHullMarker median = null;

		// Sort on x
		Collections.sort(hull);

		if ((hull.size() % 2) != 0) {
			// odd size
			median = hull.get((hull.size() - 1) / 2);
			hull.remove((hull.size() - 1) / 2);
		}

		// Add points to two lists
		for (int i = 0; i < hull.size() / 2; i++) {
			leftSide.add(hull.get(i));
		}

		Collections.sort(leftSide, new Comparator<ConvexHullMarker>() {
			public int compare(ConvexHullMarker m1, ConvexHullMarker m2) {
				return m1.getCartesianY() - m2.getCartesianY();
			}
		});

		for (int i = hull.size() / 2; i < hull.size(); i++) {
			rightSide.add(hull.get(i));
		}

		Collections.sort(rightSide, new Comparator<ConvexHullMarker>() {
			public int compare(ConvexHullMarker m1, ConvexHullMarker m2) {
				return m2.getCartesianY() - m1.getCartesianY();
			}
		});

		ArrayList<ConvexHullMarker> res = new ArrayList<ConvexHullMarker>();
		Log.i("ConvexHull", "START: " + hull.size());
		if (median != null) {
			Log.i("ConvexHull", "Median is: " + median.getCartesianY());
			Log.i("ConvexHull",
					"LSCT is: "
							+ leftSide.get((leftSide.size()) - 1)
									.getCartesianY());
			if ((median.getCartesianY() > leftSide.get((leftSide.size()) - 1)
					.getCartesianY())
			// && (median.getCartesianY() < rightSide.get(rightSide.size()
			// -1).getCartesianY() )

			) {
				Log.i("ConvexHull", "Middle");
				res.addAll(leftSide);
				res.add(median);
				res.addAll(rightSide);
			} else {
				res.addAll(leftSide);
				res.addAll(rightSide);
				res.add(median);
				Log.i("ConvexHull", "Right");
			}
		} else {
			res.addAll(leftSide);
			res.addAll(rightSide);
			Log.i("ConvexHull", "No Median");
		}
		Log.i("ConvexHull",
				"--------------------------------------------------");
		return res;
	}

	public ArrayList<ConvexHullMarker> computeDCHull(
			ArrayList<ConvexHullMarker> startingList) {
		int i = 0;
		int k = 0;
		ArrayList<ConvexHullMarker> tempList = new ArrayList<ConvexHullMarker>();
		while (startingList.size() > 3) {
			ArrayList<ConvexHullMarker> dcList = new ArrayList<ConvexHullMarker>();
			dcList.add(startingList.get(0));
			dcList.add(startingList.get(1));
			dcList.add(startingList.get(2));
			dcList.add(startingList.get(3));
			startingList.remove(3);
			startingList.remove(2);
			startingList.remove(1);
			startingList.remove(0);

			listOfLists.add(dcList);
			i++;
		}

		while (startingList.size() > 0) {
			ArrayList<ConvexHullMarker> dcList = new ArrayList<ConvexHullMarker>();
			// dcList.add(startingList.get(j));
			dcList.add(startingList.get(0));
			startingList.remove(0);

			dcList.addAll(listOfLists.get(i - 1));

			listOfLists.set((i - 1), dcList);
		}

		// startingList.clear();
		convexHull.clear();
		while (k < listOfLists.size()) {

			tempList.clear();
			Lupper.clear();
			Llower.clear();

			ArrayList<ConvexHullMarker> backup3 = new ArrayList<ConvexHullMarker>();
			for (ConvexHullMarker obj : convexHull)
				backup3.add(obj.clone());
			convexHull.clear();
			// Add the all of the elements in the next sub list

			tempList.addAll(listOfLists.get(k));
			computeLowerHull(tempList);
			computeUpperHull(tempList);

			combineHulls();

			sortHull();
			tempList.addAll(backup3);

			ArrayList<ConvexHullMarker> backup2 = new ArrayList<ConvexHullMarker>();
			for (ConvexHullMarker obj : convexHull)
				backup2.add(obj.clone());

			tempList.addAll(backup2);
			convexHull.clear();

			ArrayList<ConvexHullMarker> tempList2 = unique(tempList);

			computeLowerHull(tempList2);
			computeUpperHull(tempList2);

			combineHulls();

			tempList.clear();
			k++;
		}

		return convexHull;
	}

	private void sortHull() {

		Set<ConvexHullMarker> set = new HashSet<ConvexHullMarker>();
		set.addAll(convexHull);
		convexHull.clear();
		convexHull.addAll(set);

		Collections.sort(this.convexHull);
	}

	private ArrayList<ConvexHullMarker> unique(ArrayList<ConvexHullMarker> input) {
		Set<ConvexHullMarker> set = new HashSet<ConvexHullMarker>();
		set.addAll(input);
		ArrayList<ConvexHullMarker> ret = new ArrayList<ConvexHullMarker>();
		ret.addAll(set);
		Collections.sort(ret);
		return ret;
	}

	private void computeUpperHull(ArrayList<ConvexHullMarker> inputList)

	{
		Lupper.clear();
		inputList = unique(inputList);
		int turn;
		Lupper.add(inputList.get(0));
		Lupper.add(inputList.get(1));

		for (int i = 2; i < inputList.size(); i++) {
			Lupper.add(inputList.get(i));

			turn = findTurn(Lupper.get((Lupper.size() - 3)),
					Lupper.get((Lupper.size() - 2)),
					Lupper.get((Lupper.size() - 1)));

			while ((Lupper.size() > 2) && (turn != -1)) {

				Lupper.remove((Lupper.size() - 2));

				if (Lupper.size() > 2) {
					// Check again to see if its a right turn
					turn = findTurn(Lupper.get((Lupper.size() - 3)),
							Lupper.get((Lupper.size() - 2)),
							Lupper.get((Lupper.size() - 1)));
				} else {

					break;
				}
			}
		}
	}

	private void computeLowerHull(ArrayList<ConvexHullMarker> inputList) {
		Llower.clear();
		inputList = unique(inputList);
		int size = inputList.size();
		int turn;

		Llower.add(inputList.get((size - 1)));
		Llower.add(inputList.get((size - 2)));

		for (int i = (size - 3); i >= 0; i--) {
			Llower.add(inputList.get(i));

			turn = findTurn(Llower.get((Llower.size() - 3)),
					Llower.get((Llower.size() - 2)),
					Llower.get((Llower.size() - 1)));

			while ((Llower.size() > 2) && (turn != -1)) {

				Llower.remove((Llower.size() - 2));

				if (Llower.size() > 2) {
					// Check again to see if its a right turn
					turn = findTurn(Llower.get((Llower.size() - 3)),
							Llower.get((Llower.size() - 2)),
							Llower.get((Llower.size() - 1)));
				}

				else {
					break;
				}
			}

		}
		Llower.remove((Llower.size() - 1));
		Llower.remove(0);
	}

	private void combineHulls() {
		convexHull.clear();
		convexHull.addAll(Lupper);
		convexHull.addAll(Llower);
		Llower.clear();
		Lupper.clear();
	}

	private int findTurn(ConvexHullMarker A, ConvexHullMarker B,
			ConvexHullMarker C) {
		// Computes the cross product
		int xProduct = (((B.getCartesianX() - A.getCartesianX()) * (C
				.getCartesianY() - A.getCartesianY())) - ((B.getCartesianY() - A
				.getCartesianY()) * (C.getCartesianX() - A.getCartesianX())));

		if (xProduct > 0) {
			return 1; // Left Turn
		}
		if (xProduct < 0) {
			return -1; // Right Turn
		}

		return 0;
	}
}
