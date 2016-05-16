package michael.popularmoviestest;

/**
 * Created by Michael on 4/2/2016.
 */
public class MovieClass
{
    private String title;
    private String ID;
    private String poster;
    private String overview;
    private String voter_average;
    private String release_date;

    public String getOverview() {
        return overview;
    }
    public String getTitle() {
        return title;
    }
    public String getID(){ return ID; }
    public String getPoster() { return poster; }
    public String getVoter_average() {return voter_average;}
    public String getRelease_date() {return release_date;}

    public MovieClass(String title, String poster, String overview, String voter_average, String release_date, String ID) {
        this.title = title;
        this.ID = ID;
        this.poster = "http://image.tmdb.org/t/p/w500/"+poster;
        this.overview = overview;
        this.voter_average = voter_average;
        this.release_date = release_date;
    }
}
