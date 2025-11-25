import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import * as React from 'react';

const riksbankenDataTypes = [
  'Styrränta Sverige',
  'USD/SEK valutakurs',
  'Svensk statsobligation 2-års löptid',
  'Svensk statsobligation 5-års löptid',
  'Svensk statsobligation 7-års löptid',
  'Svensk statsobligation 10-års löptid',
  'Svensk statsskuldväxel 1-månads löptid',
  'Svensk statsskuldväxel 3-månads löptid',
  'Svensk statsskuldväxel 6-månads löptid',
  'Svensk statsskuldväxel 12-månads löptid',
  'Euromarknadsränta Danmark 3-månaders löptid',
  'Euromarknadsränta EUR 3-månaders löptid',
  'Euromarknadsränta Storbritannien 3-månaders löptid',
  'Euromarknadsränta Japan 3-månaders löptid',
  'Euromarknadsränta Norge 3-månaders löptid',
  'Euromarknadsränta USA 3-månaders löptid',
  'Euromarknadsränta Danmark 6-månaders löptid',
  'Euromarknadsränta EUR 6-månaders löptid',
  'Euromarknadsränta Storbritannien 6-månaders löptid',
  'Euromarknadsränta Japan 6-månaders löptid',
  'Euromarknadsränta Norge 6-månaders löptid',
  'Statsobligation EUR 5-års löptid',
  'Statsobligation Frankrike 5-års löptid',
  'Statsobligation Storbritannien 5-års löptid',
  'Statsobligation Tyskland 5-års löptid',
  'Statsobligation Japan 5-års löptid',
  'Statsobligation Nederländerna 5-års löptid',
  'Statsobligation USA 5-års löptid',
  'Statsobligation Danmark 10-års löptid',
  'Statsobligation EUR 10-års löptid',
  'Statsobligation Finland 10-års löptid',
  'Statsobligation Frankrike 10-års löptid',
  'Statsobligation Storbritannien 10-års löptid',
  'Statsobligation Tyskland 10-års löptid',
  'Statsobligation Japan 10-års löptid',
  'Statsobligation Nederländerna 10-års löptid',
  'Statsobligation Norge 10-års löptid',
  'Statsobligation USA 10-års löptid',
];

const SourcesPage = () => {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <Box sx={{ flexGrow: 1 }}>
        <Grid container direction={'column'} rowSpacing={1} columnSpacing={1}>
          <Grid item>
            <Typography variant={'h1'}>Riksbanken</Typography>
          </Grid>
          {riksbankenDataTypes.map((rdt) => (
            <Grid item key={rdt}>
              <b>
                <Typography variant={'h6'}>
                  <strong>{rdt}</strong>
                </Typography>
              </b>
              <Typography>{rdt}</Typography>
            </Grid>
          ))}
        </Grid>
      </Box>
    </main>
  );
};

export default SourcesPage;
