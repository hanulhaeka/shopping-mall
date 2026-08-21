document.addEventListener("DOMContentLoaded", () => {
  const debounceTimers = new Map();

  // csrf 토큰 및 헤더 읽기
  const getCsrfHeaders = () => {
    const token = document
      .querySelector("meta[name='_csrf']")
      ?.getAttribute("content");
    const header = document
      .querySelector("meta[name='_csrf_header']")
      ?.getAttribute("content");
    return token && header ? { [header]: token } : {};
  };

  // 이벤트 위임, 수량 +, - 버튼 처리
  document.addEventListener("click", (e) => {
    const isPlus = e.target.classList.contains("btn-increase");
    const isMinus = e.target.classList.contains("btn-decrease");

    if (!isPlus && !isMinus) return;

    const row = e.target.closest(".cart-item-row");
    if (!row) return;

    const input = row.querySelector(".item-quantity-input");
    let currentQty = parseInt(input.value, 10) || 1;

    if (isPlus) {
      currentQty += 1;
    } else if (isMinus) {
      if (currentQty <= 1) return;
      currentQty -= 1;
    }

    input.value = currentQty;
    updateItemTotalPrice(row);
    updateTotalOrderPrice();

    scheduleQuantityUpdate(row, currentQty, 500);
  });

  // 디바운스 스케줄러
  function scheduleQuantityUpdate(row, targetQuantity, delay = 500) {
    const cartId = row.dataset.cartId;
    if (!cartId) return;

    if (debounceTimers.has(cartId)) {
      clearTimeout(debounceTimers.get(cartId));
    }

    const timerId = setTimeout(() => {
      sendQuantityUpdateRequest(row, cartId, targetQuantity);
      debounceTimers.delete(cartId);
    }, delay);

    debounceTimers.set(cartId, timerId);
  }

  // 비동기 수량 변경 요청
  async function sendQuantityUpdateRequest(row, cartId, quantity) {
    const buttons = row.querySelectorAll(".btn-increase, .btn-decrease");

    setButtonsDisabled(buttons, true);

    try {
      const response = await fetch("/carts/update", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...getCsrfHeaders(),
        },
        body: JSON.stringify({
          cartItemId: cartId,
          quantity: quantity,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "수량 변경에 실패했습니다.");
      }

      const data = await response.json();

      if (data && typeof data.quantity === "number") {
        row.querySelector(".item-quantity-input").value = data.quantity;
      }

      updateItemTotalPrice(row);
      updateTotalOrderPrice();
    } catch (error) {
      console.error("수량 갱신 오류:", error);
      alert(
        error.message || "수량 변경 중 문제가 발생하여 원래 상태로 복구합니다.",
      );

      await rollbackToLatestServerState(row, cartId);
    } finally {
      setButtonsDisabled(buttons, false);
    }
  }

  async function rollbackToLatestServerState(row, cartId) {
    try {
      const response = await fetch(`/carts/items/${cartId}/quantity`);
      if (response.ok) {
        const latestItem = await response.json();
        row.querySelector(".item-quantity-input").value = latestItem.quantity;
        updateItemTotalPrice(row);
        updateTotalOrderPrice();
      }
    } catch (err) {
      console.error("복구 데이터 조회 실패:", err);
    }
  }

  function setButtonsDisabled(buttons, isDisabled) {
    buttons.forEach((btn) => {
      btn.disabled = isDisabled;
      btn.classList.toggle("opacity-50", isDisabled);
    });
  }

  function updateItemTotalPrice(row) {
    const unitPrice = parseInt(row.dataset.unitPrice, 10) || 0;
    const quantity =
      parseInt(row.querySelector(".item-quantity-input")?.value, 10) || 0;
    const totalPrice = unitPrice * quantity;

    const priceEl = row.querySelector(".item-total-price");
    if (priceEl) {
      priceEl.textContent = `${totalPrice.toLocaleString("ko-KR")}원`;
    }
  }

  function updateTotalOrderPrice() {
    let total = 0;
    document.querySelectorAll(".cart-item-row").forEach((row) => {
      const unitPrice = parseInt(row.dataset.unitPrice, 10) || 0;
      const quantity =
        parseInt(row.querySelector(".item-quantity-input")?.value, 10) || 0;
      total += unitPrice * quantity;
    });

    const totalDisplay = document.querySelector(".total-order-price-display");
    if (totalDisplay) {
      totalDisplay.textContent = `총 주문 금액 : ${total.toLocaleString("ko-KR")}원`;
    }
  }
});
