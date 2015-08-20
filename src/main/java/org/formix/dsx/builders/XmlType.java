/****************************************************************************
 * Copyright 2009-2015 Jean-Philippe Gravel, P. Eng. CSDP
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.formix.dsx.builders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Tells the XmlBuilder to add the contained object type as an attribute to the
 * corresponding XmlElement. This attribute shall be set on all undefined
 * references (Abstract classes, interfaces and Object class references).
 * 
 * 
 * *** TODO: deprecate this annotation and add the following XmlBuilder behavior: if the
 * method return type is different than the returned type, add the type
 * information to the corresponding property element ***
 * 
 * @author jpgravel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XmlType {

	/**
	 * @return the namespace for primitive data types see http://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/datatypes.html#built-in-primitive-datatypes
	 * 
	 */
	String valueNameSpace() default "http://www.w3.org/2001/XMLSchema";

	/**
	 * @return where the "type" attribute is defined.
	 */
	String attributeNameSpace() default "http://www.w3.org/2001/XMLSchema-instance";

}
