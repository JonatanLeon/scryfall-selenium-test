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

    @Test
    public void validSearchQueryTest() {
        search = "gandalf";

        // 1. User makes a valid search
        enterSearch(search);
        // 2. Validates that the results page is displayed
        Assert.assertTrue(driver.findElement(By.className("search-info"))
                .isDisplayed());
        // 3. Validates the results of the search
        // Expected result: the list of results matches the user's query
        assertResults(search);
    }

    @Test
    public void invalidSearchQueryTest() {
        search = "~~@#~@#~@!!!";
        // 1. User makes an invalid search
        enterSearch(search);
        // 2. Validates the results of the search
        // Expected result: there are no results for an invalid search
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"main\"]/div[3]/div/h1"))
                .getText(), "No cards found");
        assertNoResults();
    }

    @Test
    public void emptySearchQueryTest() {
        // 1. User makes an empty search
        searchBar.sendKeys(Keys.ENTER);
        // 2. Validates the results of the search
        // Expected result: there are no results for an empty search.
        // A specific error message for that is displayed
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"main\"]/div[2]/p"))
                .getText(), "You didn‘t enter anything to search for.");
        assertNoResults();
    }

    @Test
    public void searchResultSortingTest() {
        String sortByDate = "//*[@id=\"order\"]/option[2]";

        // 1. User makes a valid search
        enterSearch("lotus");
        // 2. The type of sorting is selected after the results have appeared
        List<WebElement> dropdownMenu = driver.findElements(By.id("order"));
        dropdownMenu.get(1).click();
        driver.findElement(By.xpath(sortByDate)).click();
        // 3. Validates the sorting
        // Expected result: the chosen sorting by release date is applied
        Assert.assertEquals(driver.findElement(By.xpath(sortByDate))
                .getAttribute("selected"), "true");
    }

    @Test
    public void paginationTest() {
        search = "mage";
        String next60ResultsButton = "//*[@id=\"main\"]/div[1]/div/div[2]/a[1]";

        // 1. User makes a valid search that returns too many elements
        enterSearch(search);
        // 2. First it validates the results of the first page and if pagination is enabled
        assertResults(search);
        Assert.assertTrue(driver.findElement(By.xpath(next60ResultsButton))
                .isEnabled());
        // 3. Then it goes to the second page and validates the results
        driver.findElement(By.xpath(next60ResultsButton)).click();
        assertResults(search);
        // 4. Finally, it goes to the third page and validates the results again
        // Expected result: pagination controls are enabled
        // and every page matches the results of the search
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
