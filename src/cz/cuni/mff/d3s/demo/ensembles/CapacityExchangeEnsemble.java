package cz.cuni.mff.d3s.demo.ensembles;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.matsim.api.core.v01.Coord;

import cz.cuni.mff.d3s.deeco.annotations.CommunicationBoundary;
import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.KnowledgePathBuilder;
import cz.cuni.mff.d3s.demo.MapUtil;
import cz.cuni.mff.d3s.demo.Settings;

@Ensemble
@PeriodicScheduling(period = 1000, order = 15)
public class CapacityExchangeEnsemble {

	@Membership
	public static boolean membership(
			@In("coord.id") String cId,
			@In("member.id") String mId,
			@In("member.position") Coord mPosition,
			@In("coord.position") Coord cPosition) {

		return !mId.equals(cId) && isInTheArea(mPosition) && isInTheArea(cPosition);
	}

	@KnowledgeExchange
	public static void exchange(
			@In("coord.sensedOccupiedLinks") String cSensedOccupiedLinksStr,
			@In("coord.learntOccupiedLinks") String cLearntOccupiedLinksStr,
			@InOut("member.learntOccupiedLinks") ParamHolder<String> mLearntOccupiedLinksStr,
			@InOut("member.sensedOccupiedLinks") ParamHolder<String> mSensedOccupiedLinksStr) {
		Map<String, String> cSensedOccupiedLinks = MapUtil.stringToMap(cSensedOccupiedLinksStr);
		Map<String, String> cLearntOccupiedLinks = MapUtil.stringToMap(cLearntOccupiedLinksStr);
		Map<String, String> mLearntOccupiedLinks = MapUtil.stringToMap(mLearntOccupiedLinksStr.value);
		Map<String, String> mSensedOccupiedLinks = MapUtil.stringToMap(mSensedOccupiedLinksStr.value);
		
		Iterator<Map.Entry<String, String>> cEntries  = cSensedOccupiedLinks.entrySet().iterator();
		Map.Entry<String, String> cEntry;
		String mValueStr;
		while (cEntries.hasNext()) {
			cEntry = cEntries.next();
			mValueStr = mLearntOccupiedLinks.get(cEntry.getKey());
			if (mValueStr == null || Long.parseLong(mValueStr) < Long.parseLong(cEntry.getValue())) {
				mLearntOccupiedLinks.put(cEntry.getKey(), cEntry.getValue());
			}
		}
		
		cEntries  = cLearntOccupiedLinks.entrySet().iterator();
		while (cEntries.hasNext()) {
			cEntry = cEntries.next();
			mValueStr = mLearntOccupiedLinks.get(cEntry.getKey());
			if (mValueStr == null || Long.parseLong(mValueStr) < Long.parseLong(cEntry.getValue())) {
				mLearntOccupiedLinks.put(cEntry.getKey(), cEntry.getValue());
			}
		}
		
		//Clean learnt capacities of member. We need to do this here to obey the single-writer property.
		Iterator<Map.Entry<String, String>> mEntries  = mLearntOccupiedLinks.entrySet().iterator();
		Map.Entry<String, String> mEntry;
		while (mEntries.hasNext()) {
			mEntry = mEntries.next();
			mValueStr = mSensedOccupiedLinks.get(mEntry.getKey());
			
			if (mValueStr != null && Long.parseLong(mValueStr) > Long.parseLong(mEntry.getValue())) {
				mEntries.remove();
			}
		}
		
		mLearntOccupiedLinksStr.value = MapUtil.mapToString(mLearntOccupiedLinks);
		mSensedOccupiedLinksStr.value = MapUtil.mapToString(mSensedOccupiedLinks);
	}
	
	@CommunicationBoundary
	public static boolean boundary(KnowledgeData data, ReadOnlyKnowledgeManager sender) throws KnowledgeNotFoundException {
		KnowledgePath kpPosition = KnowledgePathBuilder.buildSimplePath("position");
		Coord senderPosition = (Coord) sender.get(Arrays.asList(kpPosition)).getValue(kpPosition);
		return isInTheArea(senderPosition);
	}
	
	private static boolean isInTheArea(Coord position) {
		if (position == null) {
			return false;
		}
		double cellSize = Settings.LINK_LENGTH * Settings.LINKS_PER_EDGE;
		double totalSize = cellSize * Settings.CELL_COUNT;
		double yMin = (totalSize / 2.0) - cellSize - Settings.LINK_LENGTH;
		double yMax = (totalSize / 2.0) + cellSize + Settings.LINK_LENGTH;
		return position.getY() > yMin && position.getY() < yMax;
	}
}
