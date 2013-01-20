package edu.ysu.itrace;

public class LinkFinder {
	private AoiRepository aoiRepository;
	private GazeRepository gazeRepository;
	
	public LinkFinder(AoiRepository aoiRepository, GazeRepository gazeRepository) {
		this.aoiRepository = aoiRepository;
		this.gazeRepository = gazeRepository;
	}
	
	public void searchForLinks(){
		// TODO search aoi and gaze repos for matches
		LinkRepository.add(new Link(null, null, null));
	}
	
	public void displayLinks() {
		for(int i = 0; i < LinkRepository.size(); i++){
			//TODO print each of the links for the user
		}
	}
}
