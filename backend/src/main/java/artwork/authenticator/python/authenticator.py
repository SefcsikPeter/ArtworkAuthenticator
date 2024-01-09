import sys
import torch
import torchvision.models as models
import torchvision.transforms as transforms
from torchvision.transforms import ToPILImage
from torch.utils.data import DataLoader, Dataset
from torchvision.io import decode_image
import torch.nn.functional as F
from torch import nn, optim
import pandas as pd
from PIL import Image
import os
import base64
import io

# Load densenet

densenet = models.densenet121(pretrained=False)

# Replace the classifier
num_ftrs = densenet.classifier.in_features
densenet.classifier = nn.Linear(num_ftrs, 24)

# Move the model to GPU if available
device = torch.device("cuda") # device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
densenet = densenet.to(device)

# Load fine-tuned model
checkpoint_path = 'C:/Users/ptsef/OneDrive/Desktop/BSC/UserInterface/template-java/backend/src/main/java/artwork/authenticator/python/model_epoch_35.pth'
checkpoint = torch.load(checkpoint_path)
densenet.load_state_dict(checkpoint['state_dict'])
epoch = checkpoint['epoch']
densenet.to(device)

# Define transformations
test_transforms = transforms.Compose([
        transforms.Resize(128),
        transforms.CenterCrop(112),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])

# Check if any arguments were passed, get image if given
image = ''
if len(sys.argv) > 1:
    for i, arg in enumerate(sys.argv[1:], start=1):
        image = arg
else:
    print("No arguments were passed.")

if image != '':
    image_data = base64.b64decode(image)
    buffer = io.BytesIO(image_data)
    image_tensor = torch.ByteTensor(list(buffer.getvalue()))
    image_tensor = decode_image(image_tensor)
    to_pil = ToPILImage()
    pil_image = to_pil(image_tensor)
    transformed_image = test_transforms(pil_image)

# Ensure the transformed image tensor has a batch dimension
# If it doesn't, add one.
if len(transformed_image.shape) == 3:
    transformed_image = transformed_image.unsqueeze(0)

# Move the tensor to the same device as the model
transformed_image = transformed_image.to(device)
densenet.eval()
# Pass the tensor through the model
with torch.no_grad():
    model_output = densenet(transformed_image)

# Apply softmax to convert logits to probabilities
probabilities = F.softmax(model_output, dim=1)

# Get the top 3 predictions and their probabilities
top3_prob, top3_indices = torch.topk(probabilities, 3)

# Convert to Python lists for easier handling
top3_prob = top3_prob[0].cpu().numpy().tolist()
top3_indices = top3_indices[0].cpu().numpy().tolist()

# Print the top 3 predictions with their probabilities
print(f"{top3_indices[0]}: {top3_prob[0]}, {top3_indices[1]}: {top3_prob[1]}, {top3_indices[2]}: {top3_prob[2]}")