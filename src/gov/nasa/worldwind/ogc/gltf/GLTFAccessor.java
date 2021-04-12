/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.ogc.gltf;

import java.nio.*;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.typescript.*;

@TypeScriptImports(imports = "./GLTFArray,./GLTFParserContext,./GLTFUtil,../../util/FloatBuffer,../../util/ByteBuffer,../../util/ShortBuffer,./GLTFRoot,./GLTFBufferView,../../geom/Vec4,../../avlist/AVListImpl")

public class GLTFAccessor extends GLTFArray {

    public static final int COMPONENT_BYTE = 5120; // (BYTE) 	1
    public static final int COMPONENT_UNSIGNED_BYTE = 5121; //(UNSIGNED_BYTE) 	1
    public static final int COMPONENT_SHORT = 5122; // (SHORT) 	2
    public static final int COMPONENT_UNSIGNED_SHORT = 5123; // (UNSIGNED_SHORT) 	2
    public static final int COMPONENT_UNSIGNED_INT = 5125; // (UNSIGNED_INT) 	4
    public static final int COMPONENT_FLOAT = 5126; // (FLOAT) 	4

    private int bufferView;
    private int byteOffset;
    private int componentType;
    private int count;
    private String type;
    private double[] max;
    private double[] min;

    public GLTFAccessor(AVListImpl properties) {
        super();
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_BUFFER_VIEW:
                    this.bufferView = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_BYTE_OFFSET:
                    this.byteOffset = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_COMPONENT_TYPE:
                    this.componentType = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_COUNT:
                    this.count = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_TYPE:
                    this.type = properties.getValue(propName).toString();
                    break;
                case GLTFParserContext.KEY_MAX:
                    this.max = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MIN:
                    this.min = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFAccessor: Unsupported " + propName);
                    break;
            }
        }
    }

    @TypeScript(substitute = "viewBuffer.put(|viewBuffer.putUint8Array(")
    private ByteBuffer retrieveByteBuffer(GLTFRoot root) {
        GLTFBufferView view = root.getBufferViewForIdx(this.bufferView);
        byte[] srcBuffer = view.getViewData(root, this.byteOffset);
        ByteBuffer viewBuffer = ByteBuffer.allocate(srcBuffer.length);
        viewBuffer.put(srcBuffer, 0, srcBuffer.length);
        viewBuffer.rewind();
        viewBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return viewBuffer;
    }

    @TypeScript(substitute = "!this.type.equals(GLTFParserContext.KEY_VEC3)|this.type!==GLTFParserContext.KEY_VEC3")
    public Vec4[] getCoordBuffer(GLTFRoot root) {
        if (!this.type.equals(GLTFParserContext.KEY_VEC3)) {
            System.out.println("GLTFAccessor: Unsupported type.");
            return null;
        }
        ByteBuffer srcBuffer = this.retrieveByteBuffer(root);
        Vec4[] ret = null;
        switch (this.componentType) {
            case GLTFAccessor.COMPONENT_FLOAT:
                FloatBuffer floatBuffer = srcBuffer.asFloatBuffer();
                ret = new Vec4[this.count];
                for (int i = 0; i < this.count; i++) {
                    float x = floatBuffer.get();
                    float y = floatBuffer.get();
                    float z = floatBuffer.get();
                    ret[i] = new Vec4(x, y, z);
                }
                break;
            default:
                System.out.println("GLTFAccessor: Unsupported buffer component type " + this.componentType);
                break;
        }
        return ret;
    }

    public int[] getBufferIndices(GLTFRoot root) {
        ByteBuffer srcBuffer = this.retrieveByteBuffer(root);
        int[] ret = null;
        switch (this.componentType) {
            case GLTFAccessor.COMPONENT_UNSIGNED_SHORT:
                ShortBuffer shortBuffer = srcBuffer.asShortBuffer();
                ret = new int[this.count];
                for (int i = 0; i < this.count; i++) {
                    ret[i] = shortBuffer.get();
                }
                break;
            case GLTFAccessor.COMPONENT_UNSIGNED_BYTE:
                ret = new int[this.count];
                for (int i = 0; i < this.count; i++) {
                    ret[i] = srcBuffer.get();
                }
                break;
            default:
                System.out.println("GLTFAccessor: Unsupported indices component type " + this.componentType);
                break;
        }
        return ret;
    }
}
