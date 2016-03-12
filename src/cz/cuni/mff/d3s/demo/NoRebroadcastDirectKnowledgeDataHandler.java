package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.network.AbstractHost;
import cz.cuni.mff.d3s.deeco.network.DataReceiver;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;

@SuppressWarnings({ "rawtypes" })
public class NoRebroadcastDirectKnowledgeDataHandler extends
		DirectKnowledgeDataHandler {

	@Override
	public void networkBroadcast(AbstractHost from, Object data,
			Map<AbstractHost, Collection<DataReceiver>> receivers) {
		for (Collection<DataReceiver> innerReceivers : receivers.values()) {
			for (DataReceiver receiver : innerReceivers) {
				receiver.checkAndReceive(data, DEFAULT_MANET_RSSI);
			}
		}
	}

}
