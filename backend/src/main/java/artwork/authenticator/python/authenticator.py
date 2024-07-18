import sys
import torch
import torchvision.models as models
import torchvision.transforms as transforms
from torchvision.transforms import ToPILImage
from torch.utils.data import DataLoader, Dataset
from torchvision.io import decode_image, read_image
import torch.nn.functional as F
from torch import nn, optim
import pandas as pd
from PIL import Image
import os
import base64
import io

# Load densenet

densenet = models.densenet121()

# Replace the classifier
num_ftrs = densenet.classifier.in_features
densenet.classifier = nn.Linear(num_ftrs, 24)

# Move the model to GPU if available
device = torch.device('cpu')
densenet = densenet.to(device)

# Load fine-tuned model
# TODO change path
checkpoint_path = 'C:/Users/ptsef/OneDrive/Desktop/BSC/UserInterface/template-java/backend/src/main/java/artwork/authenticator/python/model_epoch_35.pth'
checkpoint = torch.load(checkpoint_path, map_location=torch.device('cpu'))
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
to_pil = ToPILImage()

# Check if any arguments were passed, get image and artist label if given
image_path = ''
artist_ind = ''
# Check if at least one argument was passed (excluding the script name)
if len(sys.argv) > 1:
    image_path = sys.argv[1]  # First argument

# Check if at least two arguments were passed
if len(sys.argv) > 2:
    artist_ind = int(sys.argv[2])  # Second argument
else:
    print("Not enough arguments were passed.")

if image_path != '':
    image = read_image(image_path)
    pil_image = to_pil(image)
    if (pil_image.mode != 'RGB'):
        pil_image = pil_image.convert('RGB')
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

prob, indices = torch.topk(probabilities, 24)

# Convert to Python lists for easier handling
prob = prob[0].cpu().numpy().tolist()
indices = indices[0].cpu().numpy().tolist()

# Index of selected artist
index = indices.index(artist_ind)

print(f"{indices[0]}, {prob[0]}, {prob[index]}")