/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.lisp.ctl.impl;

import org.onosproject.lisp.msg.protocols.DefaultLispMapReply.DefaultReplyBuilder;
import org.onosproject.lisp.msg.protocols.LispEncapsulatedControl;
import org.onosproject.lisp.msg.protocols.LispMapRecord;
import org.onosproject.lisp.msg.protocols.LispMapReply;
import org.onosproject.lisp.msg.protocols.LispMapReply.ReplyBuilder;
import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.lisp.msg.types.LispIpAddress;
import org.onosproject.lisp.msg.protocols.LispMapRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * LISP map resolver class.
 * Handles map-request message and acknowledges with map-reply message.
 */
public final class LispMapResolver {

    private static final Logger log = LoggerFactory.getLogger(LispMapResolver.class);

    private LispMappingDatabase mapDb = LispMappingDatabase.getInstance();

    // non-instantiable (except for our Singleton)
    private LispMapResolver() {
    }

    public static LispMapResolver getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Handles encapsulated control message and replies with map-reply message.
     *
     * @param message encapsulated control message
     * @return map-reply message
     */
    public LispMessage processMapRequest(LispMessage message) {

        LispEncapsulatedControl ecm = (LispEncapsulatedControl) message;
        LispMapRequest request = (LispMapRequest) ecm.getControlMessage();

        // build map-reply message
        ReplyBuilder replyBuilder = new DefaultReplyBuilder();
        replyBuilder.withNonce(request.getNonce());
        replyBuilder.withIsEtr(false);
        replyBuilder.withIsSecurity(false);
        replyBuilder.withIsProbe(request.isProbe());

        List<LispMapRecord> mapRecords = mapDb.getMapRecordByEidRecords(request.getEids());

        if (mapRecords.size() == 0) {
            log.warn("Map information is not found.");
        } else {
            replyBuilder.withMapRecords(mapRecords);
        }

        LispMapReply reply = replyBuilder.build();

        if (request.getItrRlocs() != null && request.getItrRlocs().size() > 0) {
            LispIpAddress itr = (LispIpAddress) request.getItrRlocs().get(0);
            InetSocketAddress address = new InetSocketAddress(itr.getAddress().toInetAddress(),
                    ecm.innerUdp().getSourcePort());
            reply.configSender(address);
        } else {
            log.warn("No ITR RLOC is found, cannot respond back to ITR.");
        }

        return reply;
    }

    /**
     * Prevents object instantiation from external.
     */
    private static final class SingletonHelper {
        private static final String ILLEGAL_ACCESS_MSG = "Should not instantiate this class.";
        private static final LispMapResolver INSTANCE = new LispMapResolver();

        private SingletonHelper() {
            throw new IllegalAccessError(ILLEGAL_ACCESS_MSG);
        }
    }
}