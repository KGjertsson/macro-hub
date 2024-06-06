import * as React from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';

const Home = () => {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <Box sx={{ flexGrow: 1 }}>
        <Grid container>
          <Grid item>
            <Typography variant={'h1'}>Macro Hub</Typography>
          </Grid>
        </Grid>
      </Box>
    </main>
  );
};

export default Home;
