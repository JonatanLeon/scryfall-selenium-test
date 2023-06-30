package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class SearchTests {
    private WebDriver driver;
    private WebElement searchBar;
    private String search;

    /**
     * Does the setup for every test before running it.
     */
    @BeforeMethod
    public void setUp() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        driver.navigate().to("https://scryfall.com/");
        searchBar = driver.findElement(By.id("q"));
    }

    /**
     * Checks that the results of a search match what is being searched.
     */
    @Test
    public void validSearchQueryTest() {
        search = "gandalf";

        enterSearch(search);
        Assert.assertTrue(driver.findElement(By.className("search-info"))
                .isDisplayed());
        assertResults(search);
    }

    /**
     * Checks that the page can handle when there are no results for an invalid search.
     */
    @Test
    public void invalidSearchQueryTest() {
        search = "~~@#~@#~@!!!";

        enterSearch(search);
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"main\"]/div[3]/div/h1"))
                .getText(), "No cards found");
        assertNoResults();
    }

    /**
     * Checks that the page can handle when the search is empty.
     */
    @Test
    public void emptySearchQueryTest() {
        searchBar.sendKeys(Keys.ENTER);
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"main\"]/div[2]/p"))
                .getText(), "You didn‘t enter anything to search for.");
        assertNoResults();
    }

    /**
     * Checks that the results can be sorted according to the user's preferences,
     * for example by release date.
     */
    @Test
    public void searchResultSortingTest() {
        String sortByDate = "//*[@id=\"order\"]/option[2]";

        enterSearch("lotus");
        List<WebElement> dropdownMenu = driver.findElements(By.id("order"));
        dropdownMenu.get(1).click();
        driver.findElement(By.xpath(sortByDate)).click();
        Assert.assertEquals(driver.findElement(By.xpath(sortByDate))
                .getAttribute("selected"), "true");
    }

    /**
     * Checks that the results are correctly paginated when there are too many
     */
    @Test
    public void paginationTest() {
        search = "mage";
        String next60ResultsButton = "//*[@id=\"main\"]/div[1]/div/div[2]/a[1]";

        enterSearch(search);
        // First it asserts the results of the first page and if pagination is enabled
        assertResults(search);
        Assert.assertTrue(driver.findElement(By.xpath(next60ResultsButton))
                .isEnabled());
        // Then it goes to the second page and asserts
        driver.findElement(By.xpath(next60ResultsButton)).click();
        assertResults(search);
        // Finally it goes to the third page and asserts again
        driver.findElement(By.xpath("//*[@id=\"main\"]/div[1]/div/div[2]/a[3]"))
                .click();
        assertResults(search);
    }

    /**
     * Closes the window after every test
     */
    @AfterMethod
    public void close() {
        driver.close();
    }

    /**
     * Allows to search for any query in the page
     *
     * @param query the query that will go in the search field
     */
    private void enterSearch(String query) {
        searchBar.sendKeys(query);
        searchBar.sendKeys(Keys.ENTER);
    }

    /**
     * Validates if every result contains what the user is searching
     *
     * @param query the query that will go in the search field
     */
    private void assertResults(String query) {
        List<WebElement> results = driver.findElements(By.className("card-grid-item-card"));
        for (WebElement result : results) {
            Assert.assertTrue(result.getText().toLowerCase()
                    .contains(query.toLowerCase()));
        }
    }

    /**
     * Validates that a list of results is empty when an invalid search is made
     */
    private void assertNoResults() {
        List<WebElement> results = driver.findElements(By.className("card-grid-item-card"));
        Assert.assertTrue(results.isEmpty());
    }
}
