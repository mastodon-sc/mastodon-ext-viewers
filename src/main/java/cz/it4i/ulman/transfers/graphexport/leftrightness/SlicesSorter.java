package cz.it4i.ulman.transfers.graphexport.leftrightness;

import cz.it4i.ulman.transfers.graphexport.GraphExportable;
import org.joml.Vector3d;
import static cz.it4i.ulman.transfers.graphexport.Utils.createVector3d;

public class SlicesSorter extends AbstractDescendantsSorter {
	/** the south-to-north reference oriented axis */
	final Vector3d axisUp;

	/** any point that lays on the axisUp */
	final Vector3d axisPoint;

	/** see PoleSorter c'tor docs... if the angle between the triangle's and observer's
	 *  up vectors is insidre [ lrTOupThresholdAngleDeg; 180-lrTOupThresholdAngleDeg ]
	 *  then up/down relation is investigated (instead of left/right) */
	public double lrTOupThresholdAngleDeg = 60;

	/** user param: what maximal incidence angle can there be between a vector from daughter1
	 *  to this.centre and a vector from daughter1 to daughter2 so that daughter2 will be declared
	 *  to be moving *towards* the centre (establishing another cell level) rather than dividing
	 *  into a side-by-side configuration (within animal surface) */
	public double layeringLowerCutoffAngleDeg = 30;

	/** user param: quite similar to this.layeringLowerCutoffAngleDeg but to declare that daughter2
	 *  is moving *outwards* the centre */
	public double layeringUpperCutoffAngleDeg = 150; //NB: 150 = 180-30

	/** south and north positions together define a south-to-north oriented axis
	 *  that serves as a reference for observer's up-vector and as the only permited
	 *  normal vector for any examined plane; the planes are thus parallel to each
	 *  other, acting as slices */
	public SlicesSorter(final Vector3d posSouth, final Vector3d posNorth)
	{
		axisUp = new Vector3d(posNorth).sub(posSouth).normalize();
		axisPoint = new Vector3d(posSouth);

		//memorize for this.exportDebugGraphics()
		spotSouth = new Vector3d(posSouth); //NB: own copy!
		spotNorth = new Vector3d(posNorth);

		final double radToDegFactor = 180.0 / Math.PI;

		this.comparator = (d1, d2) -> {
			if (d1.equals(d2)) return 0;

			final Vector3d d1pos = createVector3d(d1);
			final Vector3d d2pos = createVector3d(d2);

			//position exactly between the two daughters
			final Vector3d centre = new Vector3d(d1pos).add(d2pos).div(2.0);
			//now, project this position onto the up axis
			final double distOnUpAxis = centre.sub(axisPoint).dot(axisUp);
			axisUp.mul(distOnUpAxis, centre).add(axisPoint);
			//NB: centre is now on the axisUp when it is pasing through the axisPoint

			//super useful shortcuts...
			final Vector3d d1tod2 = new Vector3d(d2pos).sub(d1pos).normalize();
			final Vector3d d1toc  = new Vector3d(centre).sub(d1pos).normalize();

			//layering:
			//
			//check the angle between d1->centre and d1->d2,
			//does it carry a sign of starting two layers?
			double angle_d2d1c_deg = Math.acos( d1toc.dot(d1tod2) ) *radToDegFactor;
			//d2 is closer to centre than d1
			if (angle_d2d1c_deg <= layeringLowerCutoffAngleDeg) return +1;
				//d1 is closer to centre than d2
			else if (angle_d2d1c_deg >= layeringUpperCutoffAngleDeg) return -1;
			//NB: tree of a daughter closer to the centre is drawn first (in left)

			//side-by-side configuration:
			//
			//consider a triangle/plane given by d1,d2 and c
			final Vector3d triangleUp = new Vector3d(d1tod2).cross(d1toc).normalize();

			//angle between triangle's normal and up-vector (south-to-north axis)
			double angle_upsDiff_deg = Math.acos( triangleUp.dot(axisUp) ) *radToDegFactor;
			if (angle_upsDiff_deg < lrTOupThresholdAngleDeg)
			{
				//left-right case, up vectors are nearly parallel
				//d1 is left d2
				return -1;
			}
			else if (angle_upsDiff_deg > (180-lrTOupThresholdAngleDeg))
			{
				//left-right case, up vectors are nearly opposite
				//d1 is right d2
				return +1;
			}

			//up-down case                 down  up
			return d1tod2.dot(axisUp) > 0 ? -1 : +1;
		};


		this.verboseComparator = (d1, d2) -> {
			log.info("Comparing between: "+d1.getLabel()+" and "+d2.getLabel());
			if (d1.equals(d2)) {
				log.info("... which are at the same position");
				return 0;
			}

			final Vector3d d1pos = createVector3d(d1);
			final Vector3d d2pos = createVector3d(d2);

			//position exactly between the two daughters
			final Vector3d centre = new Vector3d(d1pos).add(d2pos).div(2.0);
			//now, project this position onto the up axis
			double distOnUpAxis = centre.sub(axisPoint).dot(axisUp);
			axisUp.mul(distOnUpAxis, centre).add(axisPoint);
			//NB: centre is now on the axisUp when it is pasing through the axisPoint

			log.info("  projected centre to: "+printVector(centre));
			distOnUpAxis = new Vector3d(d1pos).add(d2pos).div(2.0)
					.sub(centre).normalize()
					.dot(axisUp);
			log.info("  cos(angle daughters and axisUp), should be zero: "+distOnUpAxis);

			//super useful shortcuts...
			final Vector3d d1tod2 = new Vector3d(d2pos).sub(d1pos).normalize();
			final Vector3d d1toc  = new Vector3d(centre).sub(d1pos).normalize();

			//layering:
			//
			//check the angle between d1->centre and d1->d2,
			//does it carry a sign of starting two layers?
			double angle_d2d1c_deg = Math.acos( d1toc.dot(d1tod2) ) *radToDegFactor;
			//d2 is closer to centre than d1
			if (angle_d2d1c_deg <= layeringLowerCutoffAngleDeg) {
				log.info("  "+d1.getLabel()+" is outer->right of "+d2.getLabel());
				return +1;
			}
			//d1 is closer to centre than d2
			else if (angle_d2d1c_deg >= layeringUpperCutoffAngleDeg) {
				log.info("  "+d1.getLabel()+" is inner->left of "+d2.getLabel());
				return -1;
			}
			//NB: tree of a daughter closer to the centre is drawn first (in left)

			log.info("  layering angle: "+angle_d2d1c_deg);

			//side-by-side configuration:
			//
			//consider a triangle/plane given by d1,d2 and c
			final Vector3d triangleUp = new Vector3d(d1tod2).cross(d1toc).normalize();

			log.info("  tUP: "+printVector(triangleUp,100));

			//angle between triangle's normal and up-vector (south-to-north axis)
			double angle_upsDiff_deg = Math.acos( triangleUp.dot(axisUp) ) *radToDegFactor;
			if (angle_upsDiff_deg < lrTOupThresholdAngleDeg)
			{
				log.info("  parallel (diff: "+angle_upsDiff_deg+" deg): "
						+d1.getLabel()+" is left of "+d2.getLabel());

				//left-right case, up vectors are nearly parallel
				//d1 is left d2
				return -1;
			}
			else if (angle_upsDiff_deg > (180-lrTOupThresholdAngleDeg))
			{
				log.info("  opposite (diff: "+(180-angle_upsDiff_deg)+" deg): "
						+d1.getLabel()+" is right of "+d2.getLabel());

				//left-right case, up vectors are nearly opposite
				//d1 is right d2
				return +1;
			}

			//up-down case
			log.info("  perpendicularity (abs ang: "+angle_upsDiff_deg+" deg), would have said: "
					+d1.getLabel()+" is "+(angle_upsDiff_deg < 90? "left":"right")+" of "+d2.getLabel());
			double upDownAngle = d1tod2.dot(axisUp);
			if (upDownAngle > 0) {
				//down
				upDownAngle = Math.acos(upDownAngle) * radToDegFactor;
				log.info("  same orientation (ang: "+upDownAngle+" deg): "
						+d1.getLabel()+" is down/left of "+d2.getLabel());
				return -1;
			} else {
				//up
				upDownAngle = Math.acos(upDownAngle) * radToDegFactor;
				log.info("  opposite orientation (ang: "+(180-upDownAngle)+" deg): "
						+d1.getLabel()+" is up/right of "+d2.getLabel());
				return +1;
			}
		};
	}

	@Override
	public void exportDebugGraphics(final GraphExportable ge)
	{
		ge.addNode("South","south at "+printVector(spotSouth,1), 0, 0,0);
		ge.addNode("North","north at "+printVector(spotNorth,1), 0, 0,0);
		//NB: south is identical to the axisPoint
	}
	private final Vector3d spotSouth,spotNorth;
}