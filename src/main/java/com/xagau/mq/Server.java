/**
 * Copyright (c) 2010 Xagau.com, Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.xagau.mq;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Server {
static boolean first = true;
    static Properties p = new Properties();
    

    public static String getProperty(String property) {

        try {
            if (first) {
                File f = new File("./server.properties");
                FileInputStream fis = new FileInputStream(f);
                p.load(fis);
                fis.close();
                first = false;
            }

            String prop = p.getProperty(property);

            return prop;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // defaults.
        if (property.equals("driver")) {
            return "com.mysql.jdbc.Driver";
        } else if (property.equals("userid")) {
            return "root";
        } else if (property.equals("password")) {
            return "";
        } else if (property.equals("connectionString")) {
            return "jdbc:mysql://localhost:3306/db";
        } else if (property.equals("spread")) {
            return "0.03";
        }

        return "<NA>";
    }

    public Server() {
        // get the address of this host.
       
    }
}
