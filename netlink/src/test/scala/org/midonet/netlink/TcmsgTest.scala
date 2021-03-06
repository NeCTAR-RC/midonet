/*
 * Copyright 2016 Midokura SARL
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
 */

package org.midonet.odp

import org.junit.runner.RunWith
import org.midonet.netlink.BytesUtil
import org.midonet.netlink.rtnetlink._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}
import org.scalatest.concurrent._

@RunWith(classOf[JUnitRunner])
class TcmsgTest extends FeatureSpec
                    with BeforeAndAfterAll
                    with Matchers
                    with ScalaFutures {
    feature("netlink msgs to program the linux kernel with tc") {
        scenario("adding a filter") {

            /*
             * This byte array was generated by executing this command:
             *
             * tc filter add dev TEST.out parent ffff: protocol ip prio 5 basic police rate 200Kbit burst 30Kb drop mtu 65535
             * in gdb then dumping the netlink request as an array of bytes.
             *
             * TEST.out is an interface with id = 161
             */
            val nlmsghdr = Array(
                120, 4, 0, 0, 44, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0).toList

            val tcmsg = Array(
                0, 0, 0, 0,
                2, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 255, 255,
                0, 3, 5, 0).toList

            val basic = Array(
                10, 0, 1, 0, 98, 97,115, 105, 99, 0, 0, 0).toList

           val tcaOptions = Array(
                72, 4, 2, 0).toList

           val tcaBasicPolice = Array(
                68, 4, 4, 0)

           val tcaPoliceStruct = Array(
                60, 0, 1, 0,

                0, 0, 0, 0,
                2, 0, 0, 0,
                0, 0, 0, 0,
                0, 159, 36, 0,
                255, 255, 0, 0,

                8,
                1,
                0, 0,
                255, 255,
                0, 0,
                168, 97, 0, 0,

                0,
                0,
                0, 0,
                0, 0,
                0, 0, 0, 0,

                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0)

            val rtab = Array(
                0, 0, 4, 4, 2, 0, 0, 113, 2, 0, 0, 226, 4, 0, 0, 83, 7, 0, 0,
                196, 9, 0, 0, 53, 12, 0, 0, 166, 14, 0, 0, 23, 17, 0, 0, 136, 19, 0, 0, 249, 21, 0, 0, 106, 24, 0, 0, 219, 26, 0, 0, 76, 29, 0, 0, 189, 31, 0, 0, 46,
                34, 0, 0, 159, 36, 0, 0, 16, 39, 0, 0, 129, 41, 0, 0, 242, 43, 0, 0, 99, 46, 0, 0, 212, 48, 0, 0, 69, 51, 0, 0, 182, 53, 0, 0, 39, 56, 0, 0, 152, 58,
                0, 0, 9, 61, 0, 0, 122, 63, 0, 0, 235, 65, 0, 0, 92, 68, 0, 0, 205, 70, 0, 0, 62, 73, 0, 0, 175, 75, 0, 0, 32, 78, 0, 0, 145, 80, 0, 0, 2, 83, 0, 0,
                115, 85, 0, 0, 228, 87, 0, 0, 85, 90, 0, 0, 198, 92, 0, 0, 55, 95, 0, 0, 168, 97, 0, 0, 25, 100, 0, 0, 138, 102, 0, 0, 251, 104, 0, 0, 108, 107, 0, 0,
                221, 109, 0, 0, 78, 112, 0, 0, 191, 114, 0, 0, 48, 117, 0, 0, 161, 119, 0, 0, 18, 122, 0, 0, 131, 124, 0, 0, 244, 126, 0, 0, 101, 129, 0, 0, 214, 131,
                0, 0, 71, 134, 0, 0, 184, 136, 0, 0, 41, 139, 0, 0, 154, 141, 0, 0, 11, 144, 0, 0, 124, 146, 0, 0, 237, 148, 0, 0, 94, 151, 0, 0, 207, 153, 0, 0, 64,
                156, 0, 0, 177, 158, 0, 0, 34, 161, 0, 0, 147, 163, 0, 0, 4, 166, 0, 0, 117, 168, 0, 0, 230, 170, 0, 0, 87, 173, 0, 0, 200, 175, 0, 0, 57, 178, 0, 0,
                170, 180, 0, 0, 27, 183, 0, 0, 140, 185, 0, 0, 253, 187, 0, 0, 110, 190, 0, 0, 223, 192, 0, 0, 80, 195, 0, 0, 193, 197, 0, 0, 50, 200, 0, 0, 163, 202,
                0, 0, 20, 205, 0, 0, 133, 207, 0, 0, 246, 209, 0, 0, 103, 212, 0, 0, 216, 214, 0, 0, 73, 217, 0, 0, 186, 219, 0, 0, 43, 222, 0, 0, 156, 224, 0, 0, 13,
                227, 0, 0, 126, 229, 0, 0, 239, 231, 0, 0, 96, 234, 0, 0, 209, 236, 0, 0, 66, 239, 0, 0, 179, 241, 0, 0, 36, 244, 0, 0, 149, 246, 0, 0, 6, 249, 0, 0,
                119, 251, 0, 0, 232, 253, 0, 0, 89, 0, 1, 0, 202, 2, 1, 0, 59, 5, 1, 0, 172, 7, 1, 0, 29, 10, 1, 0, 142, 12, 1, 0, 255, 14, 1, 0, 112, 17, 1, 0, 225,
                19, 1, 0, 82, 22, 1, 0, 195, 24, 1, 0, 52, 27, 1, 0, 165, 29, 1, 0, 22, 32, 1, 0, 135, 34, 1, 0, 248, 36, 1, 0, 105, 39, 1, 0, 218, 41, 1, 0, 75, 44,
                1, 0, 188, 46, 1, 0, 45, 49, 1, 0, 158, 51, 1, 0, 15, 54, 1, 0, 128, 56, 1, 0, 241, 58, 1, 0, 98, 61, 1, 0, 211, 63, 1, 0, 68, 66, 1, 0, 181, 68, 1,
                0, 38, 71, 1, 0, 151, 73, 1, 0, 8, 76, 1, 0, 121, 78, 1, 0, 234, 80, 1, 0, 91, 83, 1, 0, 204, 85, 1, 0, 61, 88, 1, 0, 174, 90, 1, 0, 31, 93, 1, 0,
                144, 95, 1, 0, 1, 98, 1, 0, 114, 100, 1, 0, 227, 102, 1, 0, 84, 105, 1, 0, 197, 107, 1, 0, 54, 110, 1, 0, 167, 112, 1, 0, 24, 115, 1, 0, 137, 117, 1,
                0, 250, 119, 1, 0, 107, 122, 1, 0, 220, 124, 1, 0, 77, 127, 1, 0, 190, 129, 1, 0, 47, 132, 1, 0, 160, 134, 1, 0, 17, 137, 1, 0, 130, 139, 1, 0, 243,
                141, 1, 0, 100, 144, 1, 0, 213, 146, 1, 0, 70, 149, 1, 0, 183, 151, 1, 0, 40, 154, 1, 0, 153, 156, 1, 0, 10, 159, 1, 0, 123, 161, 1, 0, 236, 163, 1,
                0, 93, 166, 1, 0, 206, 168, 1, 0, 63, 171, 1, 0, 176, 173, 1, 0, 33, 176, 1, 0, 146, 178, 1, 0, 3, 181, 1, 0, 116, 183, 1, 0, 229, 185, 1, 0, 86, 188,
                1, 0, 199, 190, 1, 0, 56, 193, 1, 0, 169, 195, 1, 0, 26, 198, 1, 0, 139, 200, 1, 0, 252, 202, 1, 0, 109, 205, 1, 0, 222, 207, 1, 0, 79, 210, 1, 0,
                192, 212, 1, 0, 49, 215, 1, 0, 162, 217, 1, 0, 19, 220, 1, 0, 132, 222, 1, 0, 245, 224, 1, 0, 102, 227, 1, 0, 215, 229, 1, 0, 72, 232, 1, 0, 185, 234,
                1, 0, 42, 237, 1, 0, 155, 239, 1, 0, 12, 242, 1, 0, 125, 244, 1, 0, 238, 246, 1, 0, 95, 249, 1, 0, 208, 251, 1, 0, 65, 254, 1, 0, 178, 0, 2, 0, 35, 3,
                2, 0, 148, 5, 2, 0, 5, 8, 2, 0, 118, 10, 2, 0, 231, 12, 2, 0, 88, 15, 2, 0, 201, 17, 2, 0, 58, 20, 2, 0, 171, 22, 2, 0, 28, 25, 2, 0, 141, 27, 2, 0,
                254, 29, 2, 0, 111, 32, 2, 0, 224, 34, 2, 0, 81, 37, 2, 0, 194, 39, 2, 0, 51, 42, 2, 0, 164, 44, 2, 0, 21, 47, 2, 0, 134, 49, 2, 0, 247, 51, 2, 0,
                104, 54, 2, 0, 217, 56, 2, 0, 74, 59, 2, 0, 187, 61, 2, 0, 44, 64, 2, 0, 157, 66, 2, 0, 14, 69, 2, 0, 127, 71, 2, 0, 240, 73, 2, 0, 97, 76, 2, 0, 210,
                78, 2, 0, 67, 81, 2, 0, 180, 83, 2, 0, 37, 86, 2, 0, 150, 88, 2, 0, 7, 91, 2, 0, 120, 93, 2, 0, 233, 95, 2, 0, 90, 98, 2, 0, 203, 100, 2, 0, 60, 103,
                2, 0, 173, 105, 2, 0, 30, 108, 2, 0, 143, 110, 2, 0, 0, 113, 2
            ).toList

            val mtu = 65535
            val rate = 200
            val burst = 30
            val ifindex = 2
            val tickInUsec = 15.625
            val buf = BytesUtil.instance.allocateDirect(5000)
            val protocol = new RtnetlinkProtocol(0)
            protocol.prepareAddPoliceFilter(buf, ifindex, rate, burst, mtu, tickInUsec)

            def getB() = buf.get() & 0xFF

            nlmsghdr.foreach(_ shouldBe getB())
            tcmsg.foreach(_ shouldBe getB())
            basic.foreach(_ shouldBe getB())
            tcaOptions.foreach(_ shouldBe getB())
            tcaBasicPolice.foreach(_ shouldBe getB())
            tcaPoliceStruct.foreach(_ shouldBe getB())
            rtab.foreach(_ shouldBe getB())
        }

        scenario("adding an ingress qdisc") {
            /*
             * This byte array was generated by executing this command:
             *
             * tc qdisc add dev TEST.out ingress
             * in gdb then dumping the netlink request as an array of bytes.
             *
             * TEST.out is an interface with id = 161
             */

            val nlmsghdr = Array(
                52, 0, 0, 0,
                36, 0,
                5, 6,
                0, 0, 0, 0,
                0, 0, 0, 0).toList

            val tcmsg = Array(
                0,
                0,
                0, 0,
                161, 0, 0, 0,
                0, 0, 255, 255,
                241, 255, 255, 255,
                0, 0, 0, 0).toList

            val ingressKind = Array(
                12, 0, 1, 0, 105, 110, 103, 114, 101, 115, 115, 0).toList

            val tcaOption = Array(
                4, 0, 2, 0).toList

            val buf = BytesUtil.instance.allocateDirect(5000)
            val protocol = new RtnetlinkProtocol(0)
            protocol.prepareAddIngressQdisc(buf, 161)

            def getB() = buf.get() & 0xFF
            nlmsghdr.foreach(_ shouldBe getB())
            tcmsg.foreach(_ shouldBe getB())
            ingressKind.foreach(_ shouldBe getB())
            tcaOption.foreach(_ shouldBe getB())
        }

        scenario("deleting an ingress qdisc") {
            /*
             * This byte array was generated by executing this command:
             *
             * tc qdisc del dev TEST.out ingress
             * in gdb then dumping the netlink request as an array of bytes.
             *
             * TEST.out is an interface with id = 161
             */

            val nlmsghdr = Array(
                52, 0, 0, 0,
                37, 0,
                5, 0,
                0, 0, 0, 0,
                0, 0, 0, 0).toList

            val tcmsg = Array(
                0,
                0,
                0, 0,
                161, 0, 0, 0,
                0, 0, 255, 255,
                241, 255, 255, 255,
                0, 0, 0, 0).toList

            val ingressKind = Array(
                12, 0, 1, 0, 105, 110, 103, 114, 101, 115, 115, 0).toList

            val tcaOption = Array(
                4, 0, 2, 0)

            val buf = BytesUtil.instance.allocateDirect(5000)
            val protocol = new RtnetlinkProtocol(0)
            protocol.prepareDeleteIngressQdisc(buf, 161)

            def getB() = buf.get() & 0xFF
            nlmsghdr.foreach(_ shouldBe getB())
            tcmsg.foreach(_ shouldBe getB())
            ingressKind.foreach(_ shouldBe getB())
            tcaOption.foreach(_ shouldBe getB())
        }
    }
}

