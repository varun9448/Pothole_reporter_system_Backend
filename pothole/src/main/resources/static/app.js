const form = document.getElementById("reportForm");
const statusEl = document.getElementById("status");
const photoInput = document.getElementById("photo");
const photoPreview = document.getElementById("photoPreview");
const addPhotoBtn = document.getElementById("addPhotoBtn");
const cameraBtn = document.getElementById("cameraBtn");
const galleryBtn = document.getElementById("galleryBtn");
const sheetBackdrop = document.getElementById("sheetBackdrop");
const photoSheet = document.getElementById("photoSheet");
const sheetCancel = document.getElementById("sheetCancel");
const locateBtn = document.getElementById("locateBtn");
const latInput = document.getElementById("latitude");
const lngInput = document.getElementById("longitude");
const MIN_REPORT_PHOTOS = 4;
const MAX_REPORT_PHOTOS = 5;

let selectedPhotos = [];
let previewUrl = "";

const setStatus = (message, isError = false) => {
  statusEl.textContent = message;
  statusEl.classList.toggle("error", isError);
};

const updatePhotoPreview = () => {
  if (previewUrl) {
    URL.revokeObjectURL(previewUrl);
    previewUrl = "";
  }

  if (!selectedPhotos.length) {
    photoPreview.textContent = "No photos selected yet. Add 4 to 5.";
    photoPreview.classList.remove("has-image");
    photoPreview.style.backgroundImage = "";
    photoPreview.dataset.count = "";
    return;
  }

  previewUrl = URL.createObjectURL(selectedPhotos[0]);
  photoPreview.textContent = "";
  photoPreview.classList.add("has-image");
  photoPreview.style.backgroundImage = `url("${previewUrl}")`;
  photoPreview.dataset.count = `${selectedPhotos.length} photo${selectedPhotos.length === 1 ? "" : "s"} selected`;
};

updatePhotoPreview();

const openSheet = () => {
  photoSheet.classList.add("open");
  sheetBackdrop.classList.add("open");
};

const closeSheet = () => {
  photoSheet.classList.remove("open");
  sheetBackdrop.classList.remove("open");
};

addPhotoBtn.addEventListener("click", () => {
  openSheet();
});

sheetBackdrop.addEventListener("click", closeSheet);
sheetCancel.addEventListener("click", closeSheet);

cameraBtn.addEventListener("click", () => {
  photoInput.setAttribute("capture", "environment");
  closeSheet();
  photoInput.click();
});

galleryBtn.addEventListener("click", () => {
  photoInput.removeAttribute("capture");
  closeSheet();
  photoInput.click();
});

photoInput.addEventListener("change", () => {
  const files = Array.from(photoInput.files || []);
  photoInput.value = "";

  if (!files.length) {
    return;
  }

  if (selectedPhotos.length + files.length > MAX_REPORT_PHOTOS) {
    setStatus(`You can upload up to ${MAX_REPORT_PHOTOS} photos.`, true);
    return;
  }

  selectedPhotos = [...selectedPhotos, ...files];
  updatePhotoPreview();
  setStatus(`${selectedPhotos.length} photo${selectedPhotos.length === 1 ? "" : "s"} selected.`);
});

locateBtn.addEventListener("click", () => {
  if (!navigator.geolocation) {
    setStatus("Location is not supported on this device.", true);
    return;
  }
  setStatus("Finding your location...");
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      latInput.value = pos.coords.latitude.toFixed(6);
      lngInput.value = pos.coords.longitude.toFixed(6);
      setStatus("Location added. You can edit it if needed.");
    },
    () => {
      setStatus("Unable to access location. You can enter it manually.", true);
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus("");

  if (!latInput.value || !lngInput.value) {
    setStatus("Please add a location before sending.", true);
    return;
  }

  if (selectedPhotos.length < MIN_REPORT_PHOTOS || selectedPhotos.length > MAX_REPORT_PHOTOS) {
    setStatus(`Please upload ${MIN_REPORT_PHOTOS} to ${MAX_REPORT_PHOTOS} photos before sending.`, true);
    return;
  }

  const payload = new FormData();
  selectedPhotos.forEach((photo) => {
    payload.append("photo", photo);
  });
  payload.append("description", form.description.value);
  payload.append("latitude", latInput.value);
  payload.append("longitude", lngInput.value);
  payload.append("reporterContact", form.reporterContact.value);

  try {
    const response = await fetch("/api/reports", {
      method: "POST",
      body: payload,
    });

    if (!response.ok) {
      const text = await response.text();
      setStatus(text || "Upload failed. Please try again.", true);
      return;
    }

    const data = await response.json();
    setStatus(`Thanks! Your report id is ${data.id}.`);
    form.reset();
    selectedPhotos = [];
    updatePhotoPreview();
  } catch (error) {
    setStatus("Network error. Please try again.", true);
  }
});
