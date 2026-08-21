package com.seungjig.shoppingmall.cart;

import com.microsoft.playwright.*;
import com.seungjig.shoppingmall.cart.entity.Cart;
import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.cart.repository.CartItemRepository;
import com.seungjig.shoppingmall.cart.repository.CartRepository;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartQuantityE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private Long savedCartItemId;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        User testUser = userRepository.save(new User("loginId", passwordEncoder.encode("password"),"userName", "010-1234-5678", Role.USER, UserStatus.ACTIVE));
        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl");
        Product testProduct = productRepository.save(product);
        Cart testCart = cartRepository.save(new Cart(testUser));
        CartItem testCartItem = cartItemRepository.save(new CartItem(testCart, testProduct, 1, testProduct.getPrice()));

        this.savedCartItemId = testCartItem.getId();

        context = browser.newContext();
        page = context.newPage();

        login(testUser.getLoginId(), "password");
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("수량 증가 버튼을 연속적으로 누르면 일정 시간 후 한번의 요청으로 DB의 실제 수량이 변경된다")
    void shouldUpdateQuantityInDatabaseAfterDebounce() {
        // /carts/update 네트워크 요청 감지 설정
        List<String> requestBodies = new ArrayList<>();
        page.onRequest(request -> {
            if (request.url().contains("/carts/update") && "POST".equalsIgnoreCase(request.method())) {
                requestBodies.add(request.postData());
            }
        });

        // 장바구니 페이지 접속
        page.navigate("http://localhost:" + port + "/carts");

        // 대상 행 및 증가 버튼 탐색
        Locator cartRow = page.locator(".cart-item-row[data-cart-id='"+ savedCartItemId + "']");
        Locator plusBtn = cartRow.locator(".btn-increase");
        Locator qtyInput = cartRow.locator(".item-quantity-input");

        
        // 디바운스 타이머 만료 후 서버 응답이 올 때까지 대기
        page.waitForResponse(
                response -> response.url().contains("/carts/update") && response.status() == 200,
                () -> {
                    plusBtn.click();
                    page.waitForTimeout(100);
                    plusBtn.click();
                    page.waitForTimeout(100);
                    plusBtn.click();
                    page.waitForTimeout(100);
                }
        );

        // UI에 수량이 즉시 반영되었는지 확인
        assertThat(qtyInput.inputValue()).isEqualTo("4");

        // 프론트엔드 네트워크 검증 - 요청이 딱 1번만 전송되었는 지
        assertThat(requestBodies).hasSize(1);
        assertThat(requestBodies.get(0)).contains("\"quantity\":4");

        // DB 실제 데이터 검증
        CartItem updatedCartItem = cartItemRepository.findById(savedCartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 존재하지 않습니다."));
        assertThat(updatedCartItem.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("수량 변경 요청 실패 시 alert 창이 뜨고 화면의 수량이 DB의 원래 수량으로 롤백된다.")
    void shouldRollbackQuantityWhenServerReturnError() {
        // alert 이벤트 리스너 등록
        List<String> dialogMessages = new ArrayList<>();
        page.onDialog(dialog -> {
            dialogMessages.add(dialog.message());
            dialog.accept();
        });

        // Playwright의 Route 기능을 사용해 /carts/update 요청 시 400에러를 반환하도록 설정
        page.route("**/carts/update", route -> route.fulfill(
                new Route.FulfillOptions()
                        .setStatus(400)
                        .setContentType("application/json")
                        .setBody("{\"message\":\"재고가 부족하여 수량을 변경할 수 없습니다.\"}")
        ));

        page.navigate("http://localhost:" + port + "/carts");

        Locator cartRow = page.locator(".cart-item-row[data-cart-id='" + savedCartItemId + "']");
        Locator plusBtn = cartRow.locator(".btn-increase");
        Locator qtyInput = cartRow.locator(".item-quantity-input");
        Locator itemTotalPrice = cartRow.locator(".item-total-price");

        assertThat(qtyInput.inputValue()).isEqualTo("1");

        page.waitForResponse(
                response -> response.url().contains("/carts/items/" + savedCartItemId + "/quantity") && response.status() == 200,
                () -> {
                    plusBtn.click();
                }
        );

        assertThat(dialogMessages).hasSize(1);
        assertThat(dialogMessages.get(0)).contains("재고가 부족하여 수량을 변경할 수 없습니다.");

        assertThat(qtyInput.inputValue()).isEqualTo("1");
        assertThat(itemTotalPrice.textContent()).isEqualTo("10,000원");

        CartItem cartItemInDb = cartItemRepository.findById(savedCartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 존재하지 않습니다."));
        assertThat(cartItemInDb.getQuantity()).isEqualTo(1);
    }

    private void login(String loginId, String password) {
        page.navigate("http://localhost:" + port + "/users/signin");
        page.fill("input[name='username']", loginId);
        page.fill("input[name='password']", password);
        page.click("button[type='submit']");
        page.waitForURL("**/products/list");
        page.navigate("http://localhost:" + port + "/carts");
    }

}
