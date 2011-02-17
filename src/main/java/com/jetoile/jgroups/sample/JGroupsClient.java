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

import java.io.IOException;

import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author khanh
 * 
 */
public class JGroupsClient extends ReceiverAdapter {

    final static private Logger LOGGER = LoggerFactory.getLogger(JGroupsClient.class);

    final private Data data = new Data();
    private RpcDispatcher rpcDispatcher;
    private Channel channel;

    public JGroupsClient(final String data) {
        this.data.setData(data);
    }

    public void stop() throws IOException {
        this.channel.close();
    }

    public void start() throws ChannelException {
        this.channel = new JChannel("default-udp.xml");

        final ChangeInfraListener changeSetListener = new ChangeInfraListener(channel);
        rpcDispatcher = new RpcDispatcher(this.channel, null, changeSetListener, this);
        changeSetListener.setRpcDispatcher(rpcDispatcher);
        this.channel.connect("privateChannel");
        this.data.setAddress(this.channel.getAddress());
    }

    public Data getData() {
        return this.data;
    }

    // @Override
    // public byte[] getState() {
    // try {
    // return Util.objectToByteBuffer(this.connector);
    // } catch (Exception e) {
    // LOGGER.error("getState error", e);
    // }
    // return null;
    // }
    //
    // @Override
    // public void setState(byte[] state) {
    // try {
    // Connector connector = (Connector)Util.objectFromByteBuffer(state);
    // connectorsStub.put(connector.getPrivateAddress(),
    // connector.getConnector());
    // } catch (Exception e) {
    // LOGGER.error("setState error", e);
    // }
    // }
}
