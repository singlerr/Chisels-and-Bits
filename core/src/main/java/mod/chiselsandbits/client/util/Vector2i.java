package mod.chiselsandbits.client.util;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Vector2i {
    public static final Codec<Vector2i> CODEC = Codec.INT.listOf().comapFlatMap((ints) -> Util.fixedSize(ints, 2).map((sizedFloats) -> new Vector2i(sizedFloats.get(0), sizedFloats.get(1))), (vector) -> ImmutableList.of(vector.x, vector.y));
    public static Vector2i XN = new Vector2i(-1.0F, 0.0F);
    public static Vector2i XP = new Vector2i(1.0F, 0.0F);
    public static Vector2i YN = new Vector2i(0.0F, -1.0F);
    public static Vector2i YP = new Vector2i(0.0F, 1.0F);
    public static Vector2i ZERO = new Vector2i(0.0F, 0.0F);
    private int x;
    private int y;

    public Vector2i() {
    }

    public Vector2i(Vector2i vector2f) {
        this.x = vector2f.x;
        this.y = vector2f.y;
    }

    public Vector2i(int a, int b) {
        this.x = a;
        this.y = b;
    }

    public Vector2i(double a, double b) {
        this.x = (int) a;
        this.y = (int) b;
    }

    public Vector2i(Vector4f pVector) {
        this(pVector.x(), pVector.y());
    }

    public Vector2i(Vector3f pVector) {
        this(pVector.x(), pVector.y());
    }

    public Vector2i(Vec3 pVector) {
        this((int)pVector.x, (int)pVector.y);
    }

    public Vector2i(Vec2 pVector) {
        this(pVector.x, pVector.y);
    }

    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else if (pOther != null && this.getClass() == pOther.getClass()) {
            Vector2i Vector2f = (Vector2i)pOther;
            if (Float.compare(Vector2f.x, this.x) != 0) {
                return false;
            } else {
                return Float.compare(Vector2f.y, this.y) != 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public void mul(int pMultiplier) {
        this.x *= pMultiplier;
        this.y *= pMultiplier;
    }

    public void mul(Vector2i pMultiplier) {
        this.x *= pMultiplier.x;
        this.y *= pMultiplier.y;
    }

    public void mul(int pMx, int pMy) {
        this.x *= pMx;
        this.y *= pMy;
    }

    public void div(int pMultiplier) {
        this.x /= pMultiplier;
        this.y /= pMultiplier;
    }

    public void div(Vector2i pMultiplier) {
        this.x /= pMultiplier.x;
        this.y /= pMultiplier.y;
    }

    public void div(int pMx, int pMy) {
        this.x /= pMx;
        this.y /= pMy;
    }

    public void clamp(Vector2i pMin, Vector2i pMax) {
        this.x = Mth.clamp(this.x, pMin.x(), pMax.x());
        this.y = Mth.clamp(this.y, pMin.x(), pMax.y());
    }

    public void clamp(int pMin, int pMax) {
        this.x = Mth.clamp(this.x, pMin, pMax);
        this.y = Mth.clamp(this.y, pMin, pMax);
    }

    public void set(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    public void load(Vector2i pOther) {
        this.x = pOther.x;
        this.y = pOther.y;
    }

    public void add(int pX, int pY) {
        this.x += pX;
        this.y += pY;
    }

    public void add(Vector2i pOther) {
        this.x += pOther.x;
        this.y += pOther.y;
    }

    public void sub(Vector2i pOther) {
        this.x -= pOther.x;
        this.y -= pOther.y;
    }

    public int dot(Vector2i pOther) {
        return this.x * pOther.x + this.y * pOther.y;
    }

    public Vector2i copy() {
        return new Vector2i(this.x, this.y);
    }

    public int lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + "]";
    }
}
