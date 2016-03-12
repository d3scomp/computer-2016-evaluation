package cz.cuni.mff.d3s.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.model.runtime.api.EnsembleDefinition;
import cz.cuni.mff.d3s.deeco.network.DefaultKnowledgeDataManager;
import cz.cuni.mff.d3s.deeco.network.IPGossipStrategy;
import cz.cuni.mff.d3s.deeco.network.KnowledgeMetaData;

public class AlwaysRebroadcastingKnowledgeDataManager extends
		DefaultKnowledgeDataManager {

	public AlwaysRebroadcastingKnowledgeDataManager(
			List<EnsembleDefinition> ensembleDefinitions,
			IPGossipStrategy ipGossipStrategy) {
		super(ensembleDefinitions, ipGossipStrategy);
	}
	
	protected int getManetRebroadcastDelay(KnowledgeMetaData metaData) {
		return maxRebroadcastDelay;
	}

}
