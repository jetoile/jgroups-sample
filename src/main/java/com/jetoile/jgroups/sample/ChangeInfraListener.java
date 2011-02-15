/*
 * Copyright (c) 2011 Khanh Tuong Maudoux <kmx.petals@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jetoile.jgroups.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author khanh
 * 
 */
public class ChangeInfraListener implements MembershipListener {

    final static private Logger LOGGER = LoggerFactory.getLogger(ChangeInfraListener.class);

    final private Map<Address, String> dataCache = Collections.synchronizedMap(new HashMap<Address, String>());

    final private Channel privateChannel;

    private RpcDispatcher rpcDispatcher;

    public ChangeInfraListener(final Channel privateChannel) {
        this.privateChannel = privateChannel;
    }

    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) {
        this.rpcDispatcher = rpcDispatcher;
    }

    @Override
    synchronized public void viewAccepted(View new_view) {
        // when a new member is up
        List<Address> newAddresses = getNewAddresses(new_view.getMembers());

        newAddresses.remove(privateChannel.getAddress());

        List<Address> ads = new ArrayList<Address>();
        for (Address ad : newAddresses) {
            if (!dataCache.containsKey(ad)) {
                ads.add(ad);
            }
        }

        if (!ads.isEmpty()) {
            MethodCall methodCall = new MethodCall("getData", new Object[] {}, new Class[] {});
            LOGGER.debug("invoke remote getData on: {}", ads);

            RspList resps = rpcDispatcher.callRemoteMethods(ads, methodCall, RequestOptions.SYNC);
            LOGGER.debug("after invoke getData - nb result {}", resps.numReceived());

            if (resps.numReceived() == 0) {
                LOGGER.debug("retry...");
                resps = rpcDispatcher.callRemoteMethods(ads, methodCall, RequestOptions.SYNC);
            }

            for (Object resp : resps.getResults()) {
                Data data = (Data) resp;
                LOGGER.debug("new data: {}", data);
                dataCache.put(data.getAddress(), data.getData());
            }
        }

        List<Address> olds = getObsoleteAddresses(new_view.getMembers());
        for (Address old : olds) {
            LOGGER.debug("remove data: {}", old);
            dataCache.remove(old);
        }
    }

    @Override
    public void suspect(Address suspected_mbr) {
        // NOTHING TO DO
    }

    @Override
    public void block() {
        // NOTHING TO DO
    }

    List<Address> getNewAddresses(Vector<Address> newMembers) {
        List<Address> result = new ArrayList<Address>();
        for (Address address : newMembers) {
            if (!this.dataCache.containsKey(address)) {
                result.add(address);
            }
        }
        return result;
    }

    List<Address> getObsoleteAddresses(Vector<Address> newMembers) {
        List<Address> result = new ArrayList<Address>();
        for (Address address : this.dataCache.keySet()) {
            if (!newMembers.contains(address)) {
                result.add(address);
            }
        }
        return result;
    }

}
